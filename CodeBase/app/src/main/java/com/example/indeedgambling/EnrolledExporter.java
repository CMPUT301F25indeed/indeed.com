package com.example.indeedgambling;
//file operations
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// date formatting
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class EnrolledExporter {
    public interface CSVExportCallback {
        void onSuccess(File csvFile);
        void onFailure(String error);
    }
    public static void exportAcceptedEntrants(Context context, Event event, CSVExportCallback callback){
        if (event == null){ //invalidate input
            callback.onFailure("Event must exist.");
            return;
        }
        if (event.getAcceptedEntrants() ==null || event.getAcceptedEntrants().isEmpty()){
            callback.onFailure("There are no accepted entrants for this event.");
            return;
        }
/*        new Thread(()) ->{ //run in background
            try{

            }*/
        }
    }
    //get event by eventID
    //get accepted entrants list from event
    //for each entrant ID get their detail: include entrant id, name, email, (phone, if exists?)
    //save to file
    //call callback w result

