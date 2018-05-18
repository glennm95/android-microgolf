package edu.uic.cs478.gmasca2.microgolf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    ArrayList<String> holesNumberList;
    static int winningHole;
    static Map<Integer,String> multiMap;
    Button startButton;
    static int lastShotPlayed = 0;
    String[] groups = new String[]{"Group 1","Group 2","Group 3", "Group 4","Group 5"};
    static int statusOfLastShot = 0;
    static ArrayList<Integer> shotsByP1;
    static ArrayList<Integer> shotsByP2;
    TextView holeNumberText;
    Handler p1Handler,p2Handler;
    Thread firstPlayer, secondPlayer;
    static int currentThread = 0;
    Handler mUIHandler;
    int count = 0;


    public static final int SHOT_PLAYED = 0;

    public static final int JACKPOT = 10;
    public static final int NEAR_MISS = 11;
    public static final int NEAR_GROUP = 12;
    public static final int BIG_MISS = 13;
    public static final int CATASTROPHE = 14;
    public static final int GAME_OVER = 15;



    @SuppressLint("HandlerLeak")



    public int getGroupOfShot(int shot) {
        Log.i("A","get Group shot method");
        int groupOfShot = 0;
        groupOfShot = (shot/10) + 1;
        return groupOfShot;
    }




    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_button); // Getting reference to Start Button

        ListView myList = (ListView) findViewById(R.id.hole_list); // Getting reference to the ListView

        holeNumberText = (TextView) findViewById(R.id.textView); // Getting reference to Hole Number Text

        multiMap = new HashMap<Integer,String>();

        holesNumberList = new ArrayList<>();

        shotsByP1 = new ArrayList<>();

        shotsByP2 = new ArrayList<>();

        winningHole = getRandomHole(50); // Setting the winning hole



        mUIHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.i("A","UI Handle Message method");
                int arg1 = msg.arg1;
                Log.i("A","arg 1 =" +arg1);
                int which = msg.arg2;
                int shotByPlayer = msg.arg1;
                switch (arg1){
                    case SHOT_PLAYED:
                        //update color of the appropriate position
                        Log.i("A","ui thread handler switch entered");
                        Message gameOver;
                        if(shotByPlayer == winningHole){
//                        sendOutcome = obtainMessage(JACKPOT);
                            // send message to the player thread that sent this above message
                            Toast.makeText(getApplicationContext(), "Jackpot! You won!",Toast.LENGTH_SHORT).show();

                            gameOver = obtainMessage(GAME_OVER);
                            p1Handler.sendMessage(gameOver);
                            p2Handler.sendMessage(gameOver);
                        }
                        else if(shotByPlayer == lastShotPlayed){
                            Toast.makeText(getApplicationContext(), "Catastrophe! Game Over",Toast.LENGTH_SHORT).show();
                        }
                        else if(getGroupOfShot(shotByPlayer) == getGroupOfShot(winningHole)){
                            Toast.makeText(getApplicationContext(), "Near Miss",Toast.LENGTH_SHORT).show();
                        }
                        else if(Math.abs(getGroupOfShot(shotByPlayer) - getGroupOfShot(winningHole))==1){
                            Toast.makeText(getApplicationContext(), "Near Group",Toast.LENGTH_SHORT).show();
                        }
                        else if (Math.abs(getGroupOfShot(shotByPlayer) - getGroupOfShot(winningHole))>1){
                            Toast.makeText(getApplicationContext(), "Big Miss",Toast.LENGTH_SHORT).show();
                        }
                }
            }
        };

        for (int i=0;i<50;i++)
        {
            holesNumberList.add(Integer.toString(i+1)); // Populating the hole number list
            if(i>=0 && i<10)                                // Separating the numbers into groups and storing in hashmap
                multiMap.put(i+1, "Group 1");
            else if (i>=10 && i<20)
                multiMap.put(i+1, "Group 2");
            else if (i>=20 && i<30)
                multiMap.put(i+1, "Group 3");
            else if (i>=30 && i<40)
                multiMap.put(i+1, "Group 4");
            else if (i>=40 && i<50)
                multiMap.put(i+1, "Group 5");
        }


        final CustomAdapter customAdapter = new CustomAdapter();
        myList.setAdapter(customAdapter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                firstPlayer = new Thread(new PlayerOne());
//                firstPlayer.start();

                firstPlayer = new Thread(new PlayerOne());
                secondPlayer = new Thread(new PlayerTwo());

                currentThread = 1;
                Log.i("1","Working 1");
                while(true) {
                    Log.i("1","Working 2");
                    if(currentThread ==1) {
                        Log.i("1","Working 3");

                        firstPlayer.start();
                        try {
                            firstPlayer.join();
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                    else if (currentThread ==2){
                        Log.i("1","Working 4");

                        secondPlayer.start();

                        try {
                            secondPlayer.join();
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                    else if (currentThread == 3){
                        Log.i("1","Working 5");
                        break;
                    }

                }

            }
        });
//
    }

    public int getRandomHole(int range){
        Log.i("A","get random hole method");
        Random rand = new Random();
        int value = rand.nextInt(range)+1;
        Log.i("A","value = "+value);
        return value;
    }

    public int randomShot(ArrayList<Integer> shotList){

        Log.i("A","random shot method");
        do {
            lastShotPlayed = getRandomHole(50);
        }
        while(shotList.contains(lastShotPlayed));

        return lastShotPlayed;
    }

    public int closeGroupShot(int shot, ArrayList<Integer> shotList){

        Log.i("A","close group method");
        String group = multiMap.get(shot);
        Log.i("A","got group =" +group);

        String adjacentGroup1 = null,adjacentGroup2 = null;
        for(int i = 0;i<groups.length;i++){
            if(i==0 && groups[i]==group){
                adjacentGroup1 = groups[i+1];
            }
            else if(i>0 && i<groups.length -1 && groups[i]==group){
                adjacentGroup1 = groups[i-1];
                adjacentGroup2 = groups[i+1];
            }
            else if(i==groups.length-1 && groups[i]==group){
                adjacentGroup1 = groups[i-1];
            }
        }
        ArrayList<Integer> closeGroupList = new ArrayList<Integer>();
        for(Map.Entry<Integer,String> pair: multiMap.entrySet()){
            if (pair.getValue() == group || pair.getValue() == adjacentGroup1 || pair.getValue() == adjacentGroup2){
                closeGroupList.add(pair.getKey());
            }

        Log.i("A","close group list size = "+closeGroupList.size());

        do{
            lastShotPlayed = closeGroupList.get(getRandomHole(closeGroupList.size()-1));
        }
        while (shotList.contains(lastShotPlayed));

        }
        Log.i("A","Last shot played = "+lastShotPlayed);

        return lastShotPlayed;
    }

    public int sameGroupShot(int shot, ArrayList<Integer> shotList){

        Log.i("A","same group method");
        String group = multiMap.get(shot);
        Log.i("A","got group =" +group);
        ArrayList<Integer> sameGroupList = new ArrayList<Integer>();
        for(Map.Entry<Integer,String> pair: multiMap.entrySet()){
            if (pair.getValue() == group){
                sameGroupList.add(pair.getKey());
            }
            Log.i("A","same group list size = "+sameGroupList.size());
            do{
                lastShotPlayed = sameGroupList.get(getRandomHole(sameGroupList.size()-1));
            }
            while (shotList.contains(lastShotPlayed));
        }
        return lastShotPlayed;
    }


    public class PlayerOne implements Runnable{

        int p1Shot;

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            count++;
            Log.i("A","Player one thread entered");

            try {
                Log.i("1","Waiting for 2s");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted!");
            }

            int chooseShot = getRandomHole(3);
            Log.i("A","chosen shot = "+chooseShot);
            if(count == 1)
                p1Shot = randomShot(shotsByP1);
            else {
                switch (chooseShot) {
                    case 1:
                        p1Shot = randomShot(shotsByP1);
                        break;
                    case 2:
                        p1Shot = closeGroupShot(lastShotPlayed, shotsByP1);
                        break;
                    case 3:
                        p1Shot = sameGroupShot(lastShotPlayed, shotsByP1);
                        break;
                }
            }
            shotsByP1.add(p1Shot);

            Log.i("A","p1 shot = "+p1Shot);

            Message msg = mUIHandler.obtainMessage(SHOT_PLAYED);
            msg.arg1 = p1Shot;
            msg.arg2 = 1;
            mUIHandler.sendMessage(msg);
            Log.i("A","Message sent about shot played");

            if(currentThread!=3)
                currentThread = 2;
            Looper.prepare();
            Log.i("1","Working 10");
            p1Handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    switch (what){
                        case GAME_OVER:
                            currentThread =3;
                            firstPlayer.interrupt();
                            break;
                    }
                }
            };
            Looper.loop();
            Looper.myLooper().quit();
        }
    }

    public class PlayerTwo implements Runnable{

        int p2Shot;
        @SuppressLint("HandlerLeak")
        @Override
        public void run() {




            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                System.out.println("Thread interrupted!") ;
            }

            int chooseShot = getRandomHole(3);
            switch (chooseShot){
                case 1:
                    p2Shot = randomShot(shotsByP2);
                    break;
                case 2:
                    p2Shot = closeGroupShot(lastShotPlayed, shotsByP2);
                    break;
                case 3:
                    p2Shot = sameGroupShot(lastShotPlayed, shotsByP2);
                    break;
            }
            shotsByP2.add(p2Shot);

            Message msg = mUIHandler.obtainMessage(MainActivity.SHOT_PLAYED);
            msg.arg1 = p2Shot;
            msg.arg2 = 2;
            mUIHandler.sendMessage(msg);

            if(currentThread!=3)
                currentThread =1;

            Looper.prepare();
            p2Handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    switch (what){
                        case GAME_OVER:
                            currentThread = 3;
                            secondPlayer.interrupt();
                            break;
                    }
                }
            };
            Looper.loop();
            Looper.myLooper().quit();
        }

    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return holesNumberList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.hole_item,null);

            if(i==winningHole)
                view.setBackgroundColor(Color.GREEN);

            TextView myTextView = (TextView) view.findViewById(R.id.textView);
            myTextView.setText(holesNumberList.get(i));
            return view;
        }
    }
}
