// DetailFragment.java
// Fragment subclass that displays one contact's details
package com.deitel.addressbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deitel.addressbook.data.DatabaseDescription.Contact;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DetailFragment extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

   // callback methods implemented by MainActivity
   public interface DetailFragmentListener {
      void onContactDeleted(); // called when a contact is deleted

      // pass Uri of contact to edit to the DetailFragmentListener
      void onEditContact(Uri contactUri);

       // pass Uri of contact to rate the DetailFragmentListener
       void onRatingClicked(Uri contactUri);
   }

   private static final int CONTACT_LOADER = 0; // identifies the Loader

   private DetailFragmentListener listener; // MainActivity
   private Uri contactUri; // Uri of selected contact

    private TextView userNameTextView; // displays contact's name
   private TextView nameTextView; // displays contact's name
   private TextView phoneTextView; // displays contact's phone
   private TextView emailTextView; // displays contact's email
   private TextView categoryTextView;
   private TextView titleTextView; // displays contact's street
   private TextView questionTextView; // displays contact's city
   private TouchImageView ivImage;
    private TextView ratingTextView;
    private TextView statusTextView;
   private TextView timeAskedTextView;
   private TextView timeClosedTextView;
    //private TextView timeElapsedTextView;
    public final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
    private int idQuestion;

    // set DetailFragmentListener when fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      listener = (DetailFragmentListener) context;
   }

   // remove DetailFragmentListener when fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   // called when DetailFragmentListener's view needs to be created
   @Override
   public View onCreateView(
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // this fragment has menu items to display

      // get Bundle of arguments then extract the contact's Uri
      Bundle arguments = getArguments();

      if (arguments != null)
         contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);

      // inflate DetailFragment's layout
      View view =
         inflater.inflate(R.layout.fragment_detail, container, false);

      // get the EditTexts
       //TODO add user name in detail fragment
       userNameTextView = (TextView) view.findViewById(R.id.userNameTextView);
      nameTextView = (TextView) view.findViewById(R.id.nameTextView);
      phoneTextView = (TextView) view.findViewById(R.id.phoneTextView);
      emailTextView = (TextView) view.findViewById(R.id.emailTextView);
      categoryTextView = (TextView) view.findViewById(R.id.categoryTextView);
      titleTextView = (TextView) view.findViewById(R.id.titleTextView);
      questionTextView = (TextView) view.findViewById(R.id.questionTextView);
      ivImage = (TouchImageView) view.findViewById(R.id.ivImage);
       ratingTextView = (TextView) view.findViewById(R.id.ratingTextView);
       statusTextView = (TextView) view.findViewById(R.id.statusTextView);

       timeAskedTextView = (TextView) view.findViewById(R.id.timeAskedTextView);
       timeClosedTextView = (TextView) view.findViewById(R.id.timeClosedTextView);
       //timeElapsedTextView = (TextView) view.findViewById(R.id.timeElapsedTextView);

      // load the contact
      getLoaderManager().initLoader(CONTACT_LOADER, null, this);
      return view;


   }

   // display this fragment's menu items
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_details_menu, menu);
   }

   // handle menu item selections
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.action_edit:
            listener.onEditContact(contactUri); // pass Uri to listener
            return true;
         case R.id.action_delete:
            deleteContact();
            return true;
          case R.id.action_rating:
              listener.onRatingClicked(contactUri); // pass Uri to listener
              return true;
      }

      return super.onOptionsItemSelected(item);
   }

   // delete a contact
   private void deleteContact() {
      // use FragmentManager to display the confirmDelete DialogFragment
      confirmDelete.show(getFragmentManager(), "confirm delete");
   }

   // DialogFragment to confirm deletion of contact
   private final DialogFragment confirmDelete =
      new DialogFragment() {
         // create an AlertDialog and return it
         @Override
         public Dialog onCreateDialog(Bundle bundle) {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder =
               new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete,
               new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(
                     DialogInterface dialog, int button) {

                     // use Activity's ContentResolver to invoke
                     // delete on the QuestionDatabaseContentProvider
                     getActivity().getContentResolver().delete(
                        contactUri, null, null);
                     listener.onContactDeleted(); // notify listener
                  }
               }
            );

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create(); // return the AlertDialog
         }
      };

   // called by LoaderManager to create a Loader
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      // create an appropriate CursorLoader based on the id argument;
      // only one Loader in this fragment, so the switch is unnecessary
      CursorLoader cursorLoader;

      switch (id) {
         case CONTACT_LOADER:
            cursorLoader = new CursorLoader(getActivity(),
               contactUri, // Uri of contact to display
               null, // null projection returns all columns
               null, // null selection returns all rows
               null, // no selection arguments
               null); // sort order
            break;
         default:
            cursorLoader = null;
            break;
      }

      return cursorLoader;
   }

   // called by LoaderManager when loading completes
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      // if the contact exists in the database, display its data
      if (data != null && data.moveToFirst()) {
         // get the column index for each data item
          int userNameIndex = data.getColumnIndex(Contact.COLUMN_USER_NAME);
         int nameIndex = data.getColumnIndex(Contact.COLUMN_NAME);
         int phoneIndex = data.getColumnIndex(Contact.COLUMN_PHONE);
         int emailIndex = data.getColumnIndex(Contact.COLUMN_EMAIL);
         int categoryIndex = data.getColumnIndex(Contact.COLUMN_CATEGORY);
         int titleIndex = data.getColumnIndex(Contact.COLUMN_TITLE);
         int questionIndex = data.getColumnIndex(Contact.COLUMN_QUESTION);
         //JOE: get the column index
         int photoIndex = data.getColumnIndex(Contact.COLUMN_PHOTO);
          int ratingIndex = data.getColumnIndex(Contact.COLUMN_RATING);
          int statusIndex = data.getColumnIndex(Contact.COLUMN_STATUS);
          int timeAskedIndex = data.getColumnIndex(Contact.COLUMN_TIME_ASKED);
          int timeClosedIndex = data.getColumnIndex(Contact.COLUMN_TIME_CLOSED);
          int timeElapsedIndex = data.getColumnIndex(Contact.COLUMN_TIME_ELAPSED);
          idQuestion = data.getColumnIndex(Contact._ID);

         // fill TextViews with the retrieved data
          userNameTextView.setText(data.getString(userNameIndex));
         nameTextView.setText(data.getString(nameIndex));
         phoneTextView.setText(data.getString(phoneIndex));
         emailTextView.setText(data.getString(emailIndex));
         categoryTextView.setText(data.getString(categoryIndex));
         titleTextView.setText(data.getString(titleIndex));
         questionTextView.setText(data.getString(questionIndex));
         //Joe: set retrieved image
         //Joe: Create a utility class
         DbBitmapUtility converter = new DbBitmapUtility();


          if(data.getBlob(photoIndex)!=null) {
              ivImage.setImageBitmap(
                      converter.getImage(data.getBlob(photoIndex)));
          }

          //Joe: get rating and status
          ratingTextView.setText(Integer.toString(data.getInt(ratingIndex)));

          if (data.getInt(statusIndex) == 1)
          {
              statusTextView.setText("Closed");
          }
          else
          {
              statusTextView.setText("Open");
          }
          Date resultdate = new Date(data.getInt(timeAskedIndex));
          timeAskedTextView.setText(sdf.format(resultdate));

          if (data.getInt(timeClosedIndex) == 0)
          {
              timeClosedTextView.setText("");
          }
          else
          {
              timeClosedTextView.setText(sdf.format(new Date(data.getInt(timeClosedIndex))));
          }
            /*ELASPED TIME
          if (data.getInt(timeClosedIndex) == 0)
          {
              timeElapsedTextView.setText("");
          }
          else
          {
              long diffInMis = new Date(data.getInt(timeClosedIndex)).getTime() - new Date(data.getInt(timeAskedIndex)).getTime();
              //long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMis);
              //long diffInHour = TimeUnit.MILLISECONDS.toHours(diffInMis);
              long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMis);
              timeElapsedTextView.setText(String.valueOf(diffInDays));
          }
          **/
      }
   }
    //TODO Pass the id to IM
    public int getQuestionID ()
    {
        return idQuestion;
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
