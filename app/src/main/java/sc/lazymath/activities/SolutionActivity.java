package sc.lazymath.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sc.lazymath.R;
import sc.lazymath.entities.WolframAlphaPod;
import sc.lazymath.util.ImageDownloader;

/**
 * Class
 */
public class SolutionActivity extends ActionBarActivity {

    public static final String WA_API_KEY = "K352W8-Y3RUYKRW2K";
    public static final String DEFAULT_INPUT = "f(x)=x^2";

    private EditText mathProblemEditText;
    private ExpandableListView podExpandableListView;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder alerDialogBuilder;

    private WAQuery wolframAlphaQuery;
    private WAQuery wolframAlphaRootQuery;
    private WAEngine wolframAlphaEngine;
    private List<String> podTitles;
    private Map<String, List<WolframAlphaPod>> podContent;
    private String[] excludeFromWolframAlpha = new String[]{"Input interpretation", "Root plot", "Number line"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mathProblemEditText = (EditText) findViewById(R.id.mathProblemEditText);
        mathProblemEditText.setText(DEFAULT_INPUT);
        podExpandableListView = (ExpandableListView) findViewById(R.id.podExpandableListView);

        progressDialog = new ProgressDialog(this);
        alerDialogBuilder = new AlertDialog.Builder(this);
        alerDialogBuilder.setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mathProblemEditText.requestFocus();
                    }
                });

        podTitles = new ArrayList<String>();
        podContent = new HashMap<String, List<WolframAlphaPod>>();

        wolframAlphaEngine = new WAEngine();
        wolframAlphaEngine.setAppID(WA_API_KEY);
        wolframAlphaEngine.addFormat("plaintext");
        wolframAlphaEngine.addFormat("image");

        wolframAlphaQuery = wolframAlphaEngine.createQuery();
        wolframAlphaRootQuery = wolframAlphaEngine.createQuery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_solution, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param view
     */
    public void findSolutionOnClick(View view) {
        mathProblemEditText.clearFocus();
        String input = mathProblemEditText.getText().toString();
        if (input.isEmpty()) {
            AlertDialog dialog = alerDialogBuilder.setTitle("Empty text field")
                    .setMessage("Please provide math problem")
                    .create();
            dialog.show();
            return;
        }

        WolframAlphaTask wa = new WolframAlphaTask();
        wolframAlphaQuery.setInput("roots " + input);
        wolframAlphaRootQuery.setInput(input);

        wa.execute(wolframAlphaQuery, wolframAlphaRootQuery);
    }

    /**
     * Performs given WalframAlpha query and parses XML response using {@link android.os.AsyncTask}
     */
    private class WolframAlphaTask extends AsyncTask<WAQuery, Void, Boolean> {

        public WolframAlphaTask() {
            progressDialog.setTitle("Doing math!");
            progressDialog.setMessage("Wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(WAQuery... queries) {
            boolean result = true;

            podTitles.clear();
            podContent.clear();

            try {
                for(WAQuery query : queries){
                    WAQueryResult queryResult = wolframAlphaEngine.performQuery(query);
                    result &= parse(queryResult);
                }
            } catch (WAException e) {
                e.printStackTrace();
            }

            return result;
        }

        /**
         *
         * @param queryResult
         * @return
         */
        private boolean parse(WAQueryResult queryResult) {
            boolean ret;

            if (queryResult.isError()) {
                AlertDialog dialog = alerDialogBuilder.setTitle("Query error")
                        .setMessage("error message: " + queryResult.getErrorMessage())
                        .create();
                dialog.show();

                ret = false;
            } else if (!queryResult.isSuccess()) {
                AlertDialog dialog = alerDialogBuilder.setTitle("Query error")
                        .setMessage("Query was not understood; no results available.")
                        .create();
                dialog.show();

                ret = false;
            } else {
                ret = true;

                for (WAPod waPod : queryResult.getPods()) {
                    if (!waPod.isError()) {
                        String title = waPod.getTitle();

                        if (!contains(title, excludeFromWolframAlpha)) {
                            List<WolframAlphaPod> children = new ArrayList<>();
                            String text = null;
                            String imageUrl = null;
                            Bitmap bitmap = null;

                            for (WASubpod subPod : waPod.getSubpods()) {
                                for (Object element : subPod.getContents()) {
                                    if (element instanceof WAPlainText) {
                                        text = ((WAPlainText) element).getText();
                                    } else if (element instanceof WAImage) {
                                        imageUrl = ((WAImage) element).getURL();
                                    }
                                }

                                if (hasText(imageUrl) || hasText(text)) {
                                    WolframAlphaPod wolframAlphaPod = new WolframAlphaPod(text, bitmap);

                                    children.add(wolframAlphaPod);

                                    Log.d(HomeActivity.TAG, text);
                                    Log.d(HomeActivity.TAG, imageUrl);

                                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                                        ImageDownloader imgDownloader = new ImageDownloader
                                                (wolframAlphaPod);
                                        imgDownloader.execute(imageUrl);
                                    }
                                }

                                imageUrl = null;
                                text = null;
                            }

                            if (children.size() > 0) {
                                podTitles.add(waPod.getTitle());
                                podContent.put(waPod.getTitle(), children);
                            }
                        }
                    }
                }
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                PodExpendableListAdapter ELAdapter = new PodExpendableListAdapter(SolutionActivity.this, podContent, podTitles);
                podExpandableListView.setAdapter(ELAdapter);
                progressDialog.dismiss();
                /*for (int i = 0; i < podTitles.size(); i++) {
                    String groupTitle = (String) ELAdapter.getGroup(i);
                    if (groupTitle.toLowerCase().equals("solutions") || groupTitle.toLowerCase().equals("solution") || groupTitle.toLowerCase().equals("plot")) {
                        podExpandableListView.expandGroup(i, true);
                    }
                }*/
            } else {
                alerDialogBuilder.setTitle("Error").setMessage("Somethign went wrong.").create().show();
            }
        }
    }

    private boolean contains(String text, String[] array) {
        if (hasText(text)) {
            for (int i = 0; i < array.length; i++) {
                if (array[i].toLowerCase().equals(text.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasText(String text) {
        return (text != null && !text.trim().isEmpty());
    }

}

