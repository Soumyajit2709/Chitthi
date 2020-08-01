package com.soumyajit.chitthiapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soumyajit.chitthiapp.Fragments.ChatsFragment;
import com.soumyajit.chitthiapp.Fragments.ProfileFragment;
import com.soumyajit.chitthiapp.Fragments.UsersFragment;
import com.soumyajit.chitthiapp.Model.Chat;
import com.soumyajit.chitthiapp.Model.Users;
import com.soumyajit.chitthiapp.R;
import com.soumyajit.chitthiapp.Util.ConnectionManager;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference myRef;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Chitthi");


        if (new ConnectionManager().checkConnectivity(MainActivity.this)){
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Connecting...");
            progressDialog.show();

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users users = dataSnapshot.getValue(Users.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final TabLayout tabLayout = findViewById(R.id.tabLayout);
            final ViewPager viewPager = findViewById(R.id.view_pager);

            myRef = FirebaseDatabase.getInstance().getReference("Chats");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                    int unread = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                            unread++;
                        }
                    }

                    if (unread == 0) {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    } else {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats" + "(" + unread + ")");
                    }
                    viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                    viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

                    viewPager.setAdapter(viewPagerAdapter);

                    tabLayout.setupWithViewPager(viewPager);

                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Error!!!");
            alertDialog.setMessage("No Internet Connection").setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(i);
                    finish();
                }
            })
                    .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.create();
            alertDialog.show();
        }
}

        class ViewPagerAdapter extends FragmentPagerAdapter {

            private ArrayList<Fragment> fragments;
            private ArrayList<String> titles;

            ViewPagerAdapter(FragmentManager fm) {
                super(fm);
                this.fragments = new ArrayList<>();
                this.titles = new ArrayList<>();
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            public void addFragment(Fragment fragment, String title) {
                fragments.add(fragment);
                titles.add(title);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        }

        private void status (String status) {

            if (new ConnectionManager().checkConnectivity(MainActivity.this)) {
                myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", status);
                myRef.updateChildren(hashMap);
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Error!!!");
                alertDialog.setMessage("No Internet Connection").setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(i);
                        finish();
                    }
                })
                        .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.create();
                alertDialog.show();
            }
        }

            @Override
            protected void onResume () {
                super.onResume();
                status("online");
            }


            @Override
            protected void onPause () {
                super.onPause();
                status("offline");
            }

}