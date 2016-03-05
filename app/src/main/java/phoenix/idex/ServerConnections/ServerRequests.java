package phoenix.idex.ServerConnections;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import phoenix.idex.MainActivity;
import phoenix.idex.User;

/**
 * Created by Ravinder on 3/2/16.
 */
public class ServerRequests {
    private ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://www.idex.site88.net/";

    public ServerRequests(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please Wait...");
    }

    public void storeUserDataInBackground(User user, GetUserCallBack userCallBack) {
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallBack).execute();
    }

    public void logUserInDataInBackground(User user, GetUserCallBack userCallBack) {
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallBack).execute();
    }


    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, User> {

        private User user;
        private GetUserCallBack userCallBack;

        public StoreUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected User doInBackground(Void... params) {
            User returnedUser = null;

            HashMap<String, String> userInfo = new HashMap<>();
            userInfo.put("firstname", user.getFirstname());
            userInfo.put("lastname", user.getLastname());
            userInfo.put("email", user.getEmail());
            userInfo.put("username", user.getUsername());
            userInfo.put("password", user.getPassword());

            JSONObject jObject = new JSONObject();

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/register.php");
                jObject = req.preparePost().withData(userInfo).sendAndReadJSON();

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }

            if(jObject == null) {
                // No user returned
                returnedUser = null;
            } else {
                MainActivity.isUserLoggedIn = true;
                returnedUser = new User(user.getFirstname(), user.getLastname(), user.getEmail(),
                        user.getUsername());
            }

            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            super.onPostExecute(returnedUser);
            progressDialog.dismiss();
            userCallBack.done(returnedUser);
        }
    }

    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {

        private User user;
        private GetUserCallBack userCallBack;

        public FetchUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected User doInBackground(Void... params) {


            User returnedUser = null;

            HashMap<String, String> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("password", user.getPassword());

            JSONObject jObject = new JSONObject();

            try{

                HttpRequest req = new HttpRequest("http://idex.site88.net/login.php");
                jObject = req.preparePost().withData(userInfo).sendAndReadJSON();

                if(jObject.length() == 0){
                    // No user returned
                    returnedUser = null;

                }else{
                    // Get the user details
                    String firstname = jObject.getString("firstname");
                    String lastname = jObject.getString("lastname");
                    String email = jObject.getString("email");
                    String username = jObject.getString("username");

                    returnedUser = new User(firstname, lastname, email, username);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            return returnedUser;
        }
        @Override
        protected void onPostExecute(User returnedUser){
            super.onPostExecute(returnedUser);
            progressDialog.dismiss();
            userCallBack.done(returnedUser);
        }
    }
}
