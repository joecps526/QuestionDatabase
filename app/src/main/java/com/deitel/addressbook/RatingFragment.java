package com.deitel.addressbook;// AddEditFragment.java
// Fragment for adding a new contact or editing an existing one

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.deitel.addressbook.DbBitmapUtility;
import com.deitel.addressbook.MainActivity;
import com.deitel.addressbook.R;
import com.deitel.addressbook.Utility;
import com.deitel.addressbook.data.DatabaseDescription.Contact;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RatingFragment extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

   // defines callback method implemented by MainActivity
   public interface RatingFragmentListener {
      // called when contact is saved
      void onAddEditCompleted(Uri contactUri);
   }

   // constant used to identify the Loader
   private static final int CONTACT_LOADER = 0;

   private RatingFragmentListener listener; // MainActivity
   private Uri contactUri; // Uri of selected contact



   private CoordinatorLayout coordinatorLayout; // used with SnackBars

   //Joe: variable declaration button, checkbox
   private Button btnSubmit;
   private boolean submitClicked = false;
   private CheckBox chkFinished;
   private boolean finishConfirmed = false;
   private int id;


   // set RatingFragmentListener when Fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      listener = (RatingFragmentListener) context;
   }

   // remove RatingFragmentListener when Fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   // called when Fragment's view needs to be created
   @Override
   public View onCreateView(
           //TODO DATA JAVA->NEW JAVA->LAYOUT->ADDEDIT->DETAILED category, title, body, rating, status
           //TODO RATING BAR, STATUS PAGE, build based on add/edit fragment
           //https://github.com/DreaminginCodeZH/MaterialRatingBar
           //status =  checkbox
           //use linear layout
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // fragment has menu items to display

      // inflate GUI and get references to EditTexts
      View view =
         inflater.inflate(R.layout.fragment_add_edit, container, false);
      chkFinished =
         (CheckBox) view.findViewById(R.id.checkBox);



      // set FloatingActionButton's event listener
      btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
      btnSubmit.setOnClickListener(saveRatingtButtonClicked);

      // set checkBox's event listener
      chkFinished = (CheckBox) view.findViewById(R.id.checkBox);
      chkFinished.setOnClickListener(finishedBoxChecked);



      // used to display SnackBars with brief messages
      coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(
         R.id.coordinatorLayout);

      Bundle arguments = getArguments(); // null if creating new contact

      if (arguments != null) {
         submitClicked = false;
         contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);
      }

      // if editing an existing contact, create Loader to get the contact
      if (contactUri != null)
         getLoaderManager().initLoader(CONTACT_LOADER, null, this);

      return view;
   }


   // responds to event generated when user saves a contact
   private final View.OnClickListener saveRatingtButtonClicked =
      new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             submitClicked=true;
            saveRating(); // save contact to the database
         }
      };

   private final View.OnClickListener finishedBoxChecked =
           new View.OnClickListener() {
              @Override
              public void onClick(View v) {
               // checked?
                 if (((CheckBox) v).isChecked()) {
                    Toast.makeText(getContext(),
                            "The question is answered.", Toast.LENGTH_LONG).show();
                 finishConfirmed=true;
                 }

              }
           };


   // UPDATE contact information to the database
   private void saveRating() {
      // create ContentValues object containing contact's key-value pairs

      ContentValues contentValues = new ContentValues();
      if(finishConfirmed) {
         contentValues.put(Contact.COLUMN_STATUS,
                 Integer.parseInt("1"));
      }
      contentValues.put(Contact.COLUMN_RATING,Integer.parseInt("0"));

      //default value of rating to 0 and status to 0
      contentValues.put(Contact.COLUMN_TIME_CLOSED,System.currentTimeMillis());
      contentValues.put(Contact.COLUMN_RATING,Integer.parseInt("0"));
        //refer back to addeditfragment
      if (submitClicked)  {
         // use Activity's ContentResolver to invoke
         // insert on the QuestionDatabaseContentProvider
         //myDB.update(contacts, contentValues, "_id="+id, null);
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
         int titleIndex = data.getColumnIndex(Contact.COLUMN_TITLE);
         int questionIndex = data.getColumnIndex(Contact.COLUMN_QUESTION);

         //JOE: get the column index
         int photoIndex = data.getColumnIndex(Contact.COLUMN_PHOTO);

         // fill EditTexts with the retrieved data
         //nameTextInputLayout.getEditText().setText(data.getString(nameIndex));

      }
   }

   // called by LoaderManager when the Loader is being reset
   @Override
   public void onLoaderReset(Loader<Cursor> loader) { }


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
