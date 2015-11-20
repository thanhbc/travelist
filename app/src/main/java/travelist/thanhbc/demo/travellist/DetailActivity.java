package travelist.thanhbc.demo.travellist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailActivity extends Activity implements View.OnClickListener {

    public static final String EXTRA_PARAM_ID = "place_id";
    private ListView listView;
    private ImageView imageView;
    private TextView title;
    private LinearLayout titleHolder;
    private ImageButton addButton;
    private LinearLayout revealView;
    private EditText todoInput;
    private boolean isEditTextVisible;
    private InputMethodManager methodManager;
    private Place place;
    private ArrayList<String> todoList;
    private ArrayAdapter todoAdapter;
    int defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        place = PlaceData.placeList().get(getIntent().getIntExtra(EXTRA_PARAM_ID, 0));

        listView = (ListView) findViewById(R.id.list);
        imageView = (ImageView) findViewById(R.id.placeImage);
        title = (TextView) findViewById(R.id.textView);
        titleHolder = (LinearLayout) findViewById(R.id.placeNameHolder);
        addButton = (ImageButton) findViewById(R.id.btn_add);
        revealView = (LinearLayout) findViewById(R.id.llEditTextHolder);
        todoInput = (EditText) findViewById(R.id.etTodo);

        addButton.setOnClickListener(this);
        defaultColor = getResources().getColor(R.color.primary_dark);

        methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        revealView.setVisibility(View.INVISIBLE);
        isEditTextVisible = false;

        setUpAdapter();
        loadPlace();
        windowTransition();
        getPhoto();
    }

    private void setUpAdapter() {
        todoList = new ArrayList<>();
        todoAdapter = new ArrayAdapter(this, R.layout.row_todo, todoList);
        listView.setAdapter(todoAdapter);
    }

    private void loadPlace() {
        title.setText(place.name);
        imageView.setImageResource(place.getImageResourceId(this));
    }

    private void windowTransition() {
        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                addButton.animate().alpha(1.0f);
                getWindow().getEnterTransition().removeListener(this);
            }
        });
    }

    private void addToDo(String todo) {
        todoList.add(todo);
    }

    private void getPhoto() {
        Bitmap photo = BitmapFactory.decodeResource(getResources(), place.getImageResourceId(this));
        colorize(photo);
    }

    private void colorize(Bitmap photo) {
        Palette mPalette = Palette.from(photo).generate();
        applyPalette(mPalette);
    }

    private void applyPalette(Palette mPalette) {
        getWindow().setBackgroundDrawable(new ColorDrawable(mPalette.getDarkMutedColor(defaultColor)));
        titleHolder.setBackgroundColor(mPalette.getMutedColor(defaultColor));
        revealView.setBackgroundColor(mPalette.getLightVibrantColor(defaultColor));
    }

    @Override
    public void onClick(View v) {
        Animatable animatable;
        switch (v.getId()) {
            case R.id.btn_add:
                if (!isEditTextVisible) {
                    revealEditText(revealView);
                    todoInput.requestFocus();
                    methodManager.showSoftInput(todoInput, InputMethodManager.SHOW_IMPLICIT);
                    addButton.setImageResource(R.drawable.icn_morph);
                    animatable = (Animatable) (addButton).getDrawable();
                    animatable.start();

                } else {
                    if(!TextUtils.isEmpty(todoInput.getText().toString().trim())) {
                        addToDo(todoInput.getText().toString());
                        todoAdapter.notifyDataSetChanged();
                    }
                    methodManager.hideSoftInputFromWindow(todoInput.getWindowToken(), 0);
                    hideEditText(revealView);
                    addButton.setImageResource(R.drawable.icn_morph_reverse);
                    animatable = (Animatable) (addButton).getDrawable();
                    animatable.start();
                }
        }
    }

    private void revealEditText(LinearLayout view) {
        int cx = view.getRight() - 30;
        int cy = view.getBottom() - 60;
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        view.setVisibility(View.VISIBLE);
        isEditTextVisible = true;
        anim.start();
    }

    private void hideEditText(final LinearLayout view) {
        int cx = view.getRight() - 30;
        int cy = view.getBottom() - 60;
        int initialRadius = view.getWidth();
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });
        isEditTextVisible = false;
        anim.start();
    }

    @Override
    public void onBackPressed() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(100);
        addButton.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addButton.setVisibility(View.GONE);
                finishAfterTransition();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
