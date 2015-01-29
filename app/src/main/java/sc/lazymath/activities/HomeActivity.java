package sc.lazymath.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import sc.lazymath.R;

public class HomeActivity extends ActionBarActivity {

    public static final String TAG = "sc.lazyMath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button takePhotoButton = (Button) findViewById(R.id.button_take_photo);
        takePhotoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                        startActivity(intent);
                    }
                }
        );
/*
        Bitmap example = BitmapFactory.decodeResource(this.getResources(), R.drawable.example);

        List<RasterRegion> regions = ImageUtil.regionLabeling(ImageUtil.matrixToBinary(ImageUtil
                .bitmapToMatrix(example), 200));

        for(RasterRegion region : regions){
            region.determineMoments();
        }

        List<AbstractNode> foo = MathOcr.getNthRootNodes(regions);*/
    }

    public void findSolutionOnClick(View view) {
        Intent intent = new Intent(this, SolutionActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
