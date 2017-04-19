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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;

import android.widget.RatingBar;

import android.widget.Toast;

import com.deitel.addressbook.DbBitmapUtility;
import com.deitel.addressbook.MainActivity;
import com.deitel.addressbook.R;
import com.deitel.addressbook.Utility;
import com.deitel.addressbook.data.DatabaseDescription.Contact;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class RatingFragment extends Fragment {
   // defines callback method implemented by MainActivity
    //TODO go back to mainpage LISTENER,RATING
   public interface RatingFragmentListener {
      // called when contact is saved
      void onRatingClicked(Uri contactUri);
   }

   // constant used to identify the Loader
   private static final int CONTACT_LOADER = 0;

   //private RatingFragmentListener listener; // MainActivity
   private Uri contactUri; // Uri of selected contact



   private CoordinatorLayout coordinatorLayout; // used with SnackBars

   //Joe: variable declaration button, checkbox
   private Button btnSubmit;
   private boolean submitClicked = false;
   private CheckBox chkFinished;
   private boolean finishConfirmed = false;
   private int idFromDetail;
    private RatingBar ratingBar;
    private int txtRatingValue;


   // set RatingFragmentListener when Fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      //listener = (RatingFragmentListener) context;
   }

   // remove RatingFragmentListener when Fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      //listener = null;
   }

   // called when Fragment's view needs to be created
   @Override
   public View onCreateView(

      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // fragment has menu items to display

      // inflate GUI and get references to EditTexts
      View view =
         inflater.inflate(R.layout.fragment_rating, container, false);
      chkFinished =
         (CheckBox) view.findViewById(R.id.checkBox);

      // set FloatingActionButton's event listener
      btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
      btnSubmit.setOnClickListener(saveRatingtButtonClicked);

      // set checkBox's event listener
      chkFinished = (CheckBox) view.findViewById(R.id.checkBox);
      chkFinished.setOnClickListener(finishedBoxChecked);

       //set rating bar listener
       ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
       ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
           public void onRatingChanged(RatingBar ratingBar, float rating,
                                       boolean fromUser) {

               txtRatingValue = (int)rating;

           }
       });



      // used to display SnackBars with brief messages
      coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(
         R.id.coordinatorLayout);

      Bundle arguments = getArguments(); // null if creating new contact

      if (arguments != null) {
         submitClicked = false;
         contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);
      }

      return view;
   }


   // responds to event generated when user saves a contact
   private final View.OnClickListener saveRatingtButtonClicked =
      new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Toast.makeText(getContext(),
                     "The results are saved.", Toast.LENGTH_LONG).show();
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
       else
      {
          contentValues.put(Contact.COLUMN_STATUS,
                  Integer.parseInt("0"));
      }
      contentValues.put(Contact.COLUMN_RATING,txtRatingValue);

      //default value of rating to 0 and status to 0
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       contentValues.put(Contact.COLUMN_TIME_CLOSED,sdf.format(Calendar.getInstance().getTime()));

        //refer back to addeditfragment
      if (submitClicked)  {
         // insert on the QuestionDatabaseContentProvider
         //TODO Get the id from IM
         idFromDetail = 1;
          getContext().getContentResolver().update(contactUri, contentValues, "_id="+idFromDetail, null);
         //myDB.update(contacts, contentValues, "_id="+id, null);
      }
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
