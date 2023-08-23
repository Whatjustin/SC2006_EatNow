package com.example.eatnow;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.fragment.ProfileFragment;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ImageConverter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettings extends DialogFragment implements ErrorPopUp {

    // CONSTANTS to check action code in parent fragment
    public static final int CHANGE_PROFILE = 23;
    public static final int LOG_OUT = 24;
    // CONSTANT to check successful upload profile image
    static final int UPLOAD_PROFILE = 22;
    static final String[] settings = {"Change profile picture", "Privacy", "Followers / Followings", "Display and Languages", "FAQ"};
    // Temporary storage for image
    byte[] image;
    // UIs
    ImageView exitImage;
    CircleImageView userImage;
    TextView userName;
    ListView settingsList;
    Button logoutBtn;
    ArrayAdapter<String> settingsAdapter;
    boolean change = false;
    UserAccount user = UserAccount.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.settings, container, false);

        exitImage = view.findViewById(R.id.settings_exit);
        exitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (change) {
                    Intent intent = new Intent();
                    // Callback to parent fragment
                    getTargetFragment().onActivityResult(ProfileFragment.PROFILE_FRAGMENT, CHANGE_PROFILE, intent);
                }
                getDialog().dismiss();
            }
        });

        // Setting up UIs to display details of user
        userName = view.findViewById(R.id.txtUsername);
        userImage = view.findViewById(R.id.profile_image);
        if (user.getProfile() != null) {
            try {
                Bitmap bmp = ImageConverter.convertASCIItoBitmap(user.getProfile());
                userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
            } catch (Exception e) {
            }
        }
        userName.setText(user.getUsername());

        logoutBtn = view.findViewById(R.id.btnLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Log out by setting to null
                UserAccount.setInstance(null);
                // Callback to parent fragment
                Intent intent = new Intent();
                getTargetFragment().onActivityResult(ProfileFragment.PROFILE_FRAGMENT, LOG_OUT, intent);
                getDialog().dismiss();
            }
        });

        settingsList = view.findViewById(R.id.settingsList);

        // Setting up adapter based on settings
        settingsAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.settings_list_item, settings);
        settingsList.setAdapter(settingsAdapter);

        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = adapterView.getItemAtPosition(i).toString();
                if (selected.equals(settings[0])) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, UPLOAD_PROFILE);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPLOAD_PROFILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImage);
                    image = getBytes(inputStream);
                    userImage.setImageURI(selectedImage);
                    updateUserData(image);
                    change = true;
                } catch (FileNotFoundException e) {
                    errorPopUp("Error", "Unable to upload image.");
                } catch (IOException e) {
                    errorPopUp("Error", "Unable to upload image.");
                }
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void updateUserData(byte[] image) {
        int account_id = user.getAccount_id();

        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("accounts").child(String.valueOf(account_id - 1)).child("profile");

        reference.setValue(ImageConverter.convertBinaryToASCIIString(image));

        user.setProfile(ImageConverter.convertBinaryToASCII(image));
    }

    // Construct Error Pop Up
    @Override
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }

    // Adjust Layout of Pop-up to maximize size
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
