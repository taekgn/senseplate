package kent.group8.senseplateandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;


public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> implements BackgroundWorker.TaskListener {
    private Context context;
    private DatabaseSQLite myDb;
    private String valueTest = "";

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cv;
        private TextView nameView, valueView;
        public View view;

        ViewHolder(View v) {
            super(v);
            cv = (CardView) itemView.findViewById(R.id.cv);
            nameView = (TextView)itemView.findViewById(R.id.tvAddFood);
            valueView = (TextView)itemView.findViewById(R.id.tvCalories);
            view = v;
        }
    }

    List<ProfileStorage> profileList;

    ProfileAdapter(List<ProfileStorage> profileList, Context context) {
        this.profileList = profileList;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_profile, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final ProfileStorage selected = profileList.get(i);

        viewHolder.nameView.setText(selected.name);
        if(selected.name.equals("BMI")) {
            if(!selected.value.isEmpty() && !selected.units.isEmpty()) {
                //calculate BMI using bmi formula (mass (kg) / height^2 (m))
                double bmi = Double.parseDouble(selected.units) / ((Double.parseDouble(selected.value) / 100) * (Double.parseDouble(selected.value) / 100));
                selected.value = String.format("%.2f",bmi);
                //reset units value
                selected.units = "";
            }
            else {
                selected.value = "";
            }
        }
        String[] test = new String[] {"0", "0.0", "0.00", "NaN"};
        //current work around so it doesn't show anything when user hasn't entered a value
        if(!Arrays.asList(test).contains(selected.value)) {
            viewHolder.valueView.setText(selected.value + " " + selected.units);
        }

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected.name.equals("Age") || selected.name.equals("Height") || selected.name.equals("Weight")) {
                    alertbox(selected.name, selected.value);
                }
                else if(selected.name.equals("Gender")) {
                    CharSequence itemsGoals[] = new CharSequence[] {"Male", "Female"};
                    alertboxChoice("Gender", selected.name, itemsGoals);
                }
                else if(selected.name.equals("Activity")) {
                    CharSequence itemsActivity[] = new CharSequence[] {"Inactive", "Moderately active", "Active"};
                    alertboxChoice("Activity Level", selected.name, itemsActivity);
                }
                else if(selected.name.equals("Goal")) {
                    CharSequence itemsGoals[] = new CharSequence[] {"Lose weight", "Maintain weight", "Gain weight"};
                    alertboxChoice("Weight Goal", selected.name, itemsGoals);
                }
            }
        });
    }

    //shows alertbox for option to remove food item
    public void alertbox(String name, String value) {
        myDb = new DatabaseSQLite(context);
        final String finalName = name;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(name);
        alertDialog.setMessage("Enter " + name);

        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Submit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String userInput = input.getText().toString();
                        if (!userInput.isEmpty()) {

                            myDb.updateProfileSingle(finalName, userInput);

                            Intent i = new Intent(context, ProfileActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            context.startActivity(i);
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    //shows alertbox for option to remove food item
    public void alertboxChoice(String title, String name, CharSequence[] items) {
        myDb = new DatabaseSQLite(context);
        final String finalName = name;
        final CharSequence[] finalItems = items;
        valueTest = ""; //reset valueTest
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setSingleChoiceItems(finalItems, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                valueTest = String.valueOf(finalItems[n]);
            }

        });
        alertDialog.setPositiveButton("Submit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!valueTest.isEmpty()) {
                            myDb.updateProfileSingle(finalName, valueTest);
                            Intent i = new Intent(context, ProfileActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            context.startActivity(i);
                        }
                    }
                });
        alertDialog.setNegativeButton("Cancel", null);
        alertDialog.setTitle(title);
        alertDialog.show();
    }


    public void onTaskCompleted(String result) {
    }

    @Override
    public int getItemCount () {
        return profileList.size();
    }
}
