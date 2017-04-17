// AddEditFragment.java
// Fragment for adding a new contact or editing an existing one
package com.deitel.addressbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.deitel.addressbook.data.DatabaseDescription.Contact;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddEditFragment extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

   // defines callback method implemented by MainActivity
   public interface AddEditFragmentListener {
      // called when contact is saved
      void onAddEditCompleted(Uri contactUri);
   }

   // constant used to identify the Loader
   private static final int CONTACT_LOADER = 0;

   private AddEditFragmentListener listener; // MainActivity
   private Uri contactUri; // Uri of selected contact
   private boolean addingNewContact = true; // adding (true) or editing

   // EditTexts for contact information
   private TextInputLayout nameTextInputLayout;
   private TextInputLayout phoneTextInputLayout;
   private TextInputLayout emailTextInputLayout;
   private TextInputLayout streetTextInputLayout;
   private TextInputLayout cityTextInputLayout;
   private TextInputLayout stateTextInputLayout;
   private TextInputLayout zipTextInputLayout;
   private FloatingActionButton saveContactFAB;

   private CoordinatorLayout coordinatorLayout; // used with SnackBars

   //Joe: variable declaration for photo
   private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
   private Button btnSelect;
   private ImageView ivImage;
   private String userChoosenTask;

   // set AddEditFragmentListener when Fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      listener = (AddEditFragmentListener) context;
   }

   // remove AddEditFragmentListener when Fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   // called when Fragment's view needs to be created
   @Override
   public View onCreateView(
           //TODO DATA JAVA->NEW JAVA->LAYOUT->ADDEDIT->DETAILED category, title, body, rating, status
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // fragment has menu items to display

      // inflate GUI and get references to EditTexts
      View view =
         inflater.inflate(R.layout.fragment_add_edit, container, false);
      nameTextInputLayout =
         (TextInputLayout) view.findViewById(R.id.nameTextInputLayout);
      nameTextInputLayout.getEditText().addTextChangedListener(
         nameChangedListener);
      phoneTextInputLayout =
         (TextInputLayout) view.findViewById(R.id.phoneTextInputLayout);
      emailTextInputLayout =
         (TextInputLayout) view.findViewById(R.id.emailTextInputLayout);
      streetTextInputLayout =
         (TextInputLayout) view.findViewById(R.id.streetTextInputLayout);
      cityTextInputLayout =
         (TextInputLayout) view.findViewById(R.id.cityTextInputLayout);
      stateTextInputLayout =
         (TextInputLayout) view.findViewById(R.id.stateTextInputLayout);
      zipTextInputLayout =
         (TextInputLayout) view.findViewById(R.id.zipTextInputLayout);

      //drop down list declaration
      Spinner dropdown = (Spinner) view.findViewById(R.id.spinnerCategory);
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.category));
      dropdown.setAdapter(adapter);

      //take photo declaration
      btnSelect = (Button) view.findViewById(R.id.btnSelectPhoto);
      btnSelect.setOnClickListener(new View.OnClickListener() {

         @Override
         public void onClick(View v) {
            selectImage();
         }
      });
      ivImage = (ImageView) view.findViewById(R.id.ivImage);

      // set FloatingActionButton's event listener
      saveContactFAB = (FloatingActionButton) view.findViewById(
         R.id.saveFloatingActionButton);
      saveContactFAB.setOnClickListener(saveContactButtonClicked);
      updateSaveButtonFAB();

      // used to display SnackBars with brief messages
      coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(
         R.id.coordinatorLayout);

      Bundle arguments = getArguments(); // null if creating new contact

      if (arguments != null) {
         addingNewContact = false;
         contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);
      }

      // if editing an existing contact, create Loader to get the contact
      if (contactUri != null)
         getLoaderManager().initLoader(CONTACT_LOADER, null, this);

      return view;
   }

   // detects when the text in the nameTextInputLayout's EditText changes
   // to hide or show saveButtonFAB
   private final TextWatcher nameChangedListener = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
         int after) {}

      // called when the text in nameTextInputLayout changes
      @Override
      public void onTextChanged(CharSequence s, int start, int before,
         int count) {
         updateSaveButtonFAB();
      }

      @Override
      public void afterTextChanged(Editable s) { }
   };

   // shows saveButtonFAB only if the name is not empty
   private void updateSaveButtonFAB() {
      String input =
         nameTextInputLayout.getEditText().getText().toString();

      // if there is a name for the contact, show the FloatingActionButton
      if (input.trim().length() != 0)
         saveContactFAB.show();
      else
         saveContactFAB.hide();
   }

   // responds to event generated when user saves a contact
   private final View.OnClickListener saveContactButtonClicked =
      new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // hide the virtual keyboard
            ((InputMethodManager) getActivity().getSystemService(
               Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
               getView().getWindowToken(), 0);
            saveContact(); // save contact to the database
         }
      };

   // saves contact information to the database
   private void saveContact() {
      // create ContentValues object containing contact's key-value pairs
      ContentValues contentValues = new ContentValues();
      contentValues.put(Contact.COLUMN_NAME,
         nameTextInputLayout.getEditText().getText().toString());
      contentValues.put(Contact.COLUMN_PHONE,
         phoneTextInputLayout.getEditText().getText().toString());
      contentValues.put(Contact.COLUMN_EMAIL,
         emailTextInputLayout.getEditText().getText().toString());
      contentValues.put(Contact.COLUMN_STREET,
         streetTextInputLayout.getEditText().getText().toString());
      contentValues.put(Contact.COLUMN_CITY,
         cityTextInputLayout.getEditText().getText().toString());
      contentValues.put(Contact.COLUMN_STATE,
         stateTextInputLayout.getEditText().getText().toString());
      contentValues.put(Contact.COLUMN_ZIP,
         zipTextInputLayout.getEditText().getText().toString());
      //JOE: Create a utility class
      DbBitmapUtility converter = new DbBitmapUtility();
      //JOE: Get currently saved image
      Bitmap bitmap = ((BitmapDrawable)ivImage.getDrawable()).getBitmap();
      //JOE: Convert to sqlite format
      byte[] converted = converter.getBytes(bitmap);
      contentValues.put(Contact.COLUMN_PHOTO,
              converted);

      if (addingNewContact) {
         // use Activity's ContentResolver to invoke
         // insert on the AddressBookContentProvider
         Uri newContactUri = getActivity().getContentResolver().insert(
            Contact.CONTENT_URI, contentValues);

         if (newContactUri != null) {
            Snackbar.make(coordinatorLayout,
               R.string.contact_added, Snackbar.LENGTH_LONG).show();
            listener.onAddEditCompleted(newContactUri);
         }
         else {
            Snackbar.make(coordinatorLayout,
               R.string.contact_not_added, Snackbar.LENGTH_LONG).show();
         }
      }
      else {
         // use Activity's ContentResolver to invoke
         // insert on the AddressBookContentProvider
         int updatedRows = getActivity().getContentResolver().update(
            contactUri, contentValues, null, null);

         if (updatedRows > 0) {
            listener.onAddEditCompleted(contactUri);
            Snackbar.make(coordinatorLayout,
               R.string.contact_updated, Snackbar.LENGTH_LONG).show();
         }
         else {
            Snackbar.make(coordinatorLayout,
               R.string.contact_not_updated, Snackbar.LENGTH_LONG).show();
         }
      }
   }

   // called by LoaderManager to create a Loader
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      // create an appropriate CursorLoader based on the id argument;
      // only one Loader in this fragment, so the switch is unnecessary
      switch (id) {
         case CONTACT_LOADER:
            return new CursorLoader(getActivity(),
               contactUri, // Uri of contact to display
               null, // null projection returns all columns
               null, // null selection returns all rows
               null, // no selection arguments
               null); // sort order
         default:
            return null;
      }
   }

   // called by LoaderManager when loading completes
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      // if the contact exists in the database, display its data
      if (data != null && data.moveToFirst()) {
         // get the column index for each data item
         int nameIndex = data.getColumnIndex(Contact.COLUMN_NAME);
         int phoneIndex = data.getColumnIndex(Contact.COLUMN_PHONE);
         int emailIndex = data.getColumnIndex(Contact.COLUMN_EMAIL);
         int streetIndex = data.getColumnIndex(Contact.COLUMN_STREET);
         int cityIndex = data.getColumnIndex(Contact.COLUMN_CITY);
         int stateIndex = data.getColumnIndex(Contact.COLUMN_STATE);
         int zipIndex = data.getColumnIndex(Contact.COLUMN_ZIP);
         //JOE: get the column index
         int photoIndex = data.getColumnIndex(Contact.COLUMN_PHOTO);

         // fill EditTexts with the retrieved data
         nameTextInputLayout.getEditText().setText(
            data.getString(nameIndex));
         phoneTextInputLayout.getEditText().setText(
            data.getString(phoneIndex));
         emailTextInputLayout.getEditText().setText(
            data.getString(emailIndex));
         streetTextInputLayout.getEditText().setText(
            data.getString(streetIndex));
         cityTextInputLayout.getEditText().setText(
            data.getString(cityIndex));
         stateTextInputLayout.getEditText().setText(
            data.getString(stateIndex));
         zipTextInputLayout.getEditText().setText(
            data.getString(zipIndex));
         //JOE: set retrieved image
         //JOE: Create a utility class
         DbBitmapUtility converter = new DbBitmapUtility();
         ivImage.setImageBitmap(
                 converter.getImage(data.getBlob(photoIndex)));

         updateSaveButtonFAB();
      }
   }

   // called by LoaderManager when the Loader is being reset
   @Override
   public void onLoaderReset(Loader<Cursor> loader) { }

   @Override
   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
      switch (requestCode) {
         case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               if(userChoosenTask.equals("Take Photo"))
                  cameraIntent();
               else if(userChoosenTask.equals("Choose from Library"))
                  galleryIntent();
            } else {
               //code for deny
            }
            break;
      }
   }

   private void selectImage() {
      final CharSequence[] items = { "Take Photo", "Choose from Library",
              "Cancel" };

      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
      builder.setTitle("Add Photo!");
      builder.setItems(items, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int item) {
            boolean result=Utility.checkPermission(getContext());

            if (items[item].equals("Take Photo")) {
               userChoosenTask ="Take Photo";
               if(result)
                  cameraIntent();

            } else if (items[item].equals("Choose from Library")) {
               userChoosenTask ="Choose from Library";
               if(result)
                  galleryIntent();

            } else if (items[item].equals("Cancel")) {
               dialog.dismiss();
            }
         }
      });
      builder.show();
   }

   private void galleryIntent()
   {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);//
      startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
   }

   private void cameraIntent()
   {
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      startActivityForResult(intent, REQUEST_CAMERA);
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      if (resultCode == Activity.RESULT_OK) {
         if (requestCode == SELECT_FILE)
            onSelectFromGalleryResult(data);
         else if (requestCode == REQUEST_CAMERA)
            onCaptureImageResult(data);
      }
   }

   private void onCaptureImageResult(Intent data) {
      Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

      File destination = new File(Environment.getExternalStorageDirectory(),
              System.currentTimeMillis() + ".jpg");

      FileOutputStream fo;
      try {
         destination.createNewFile();
         fo = new FileOutputStream(destination);
         fo.write(bytes.toByteArray());
         fo.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      ivImage.setImageBitmap(thumbnail);
   }

   @SuppressWarnings("deprecation")
   private void onSelectFromGalleryResult(Intent data) {

      Bitmap bm=null;
      if (data != null) {
         try {
            bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      ivImage.setImageBitmap(bm);
   }
}


/**************************************************************************
 * (C) Copyright 1992-2016 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/
