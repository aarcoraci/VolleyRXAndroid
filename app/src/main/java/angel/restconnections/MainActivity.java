package angel.restconnections;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import angel.restconnections.domain.Post;
import angel.restconnections.net.RXPostConnector;
import angel.restconnections.net.VolleyDispatcher;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    // https://github.com/amitshekhariitbhu/RxJava2-Android-Samples

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Button getPostsButton;
    private ListView postsList;
    private ArrayList<String> postTitles = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init volley
        VolleyDispatcher.getInstance().init(this);

        getPostsButton = (Button) findViewById(R.id.get_posts_button);

        getPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribeSingle();
            }
        });

        // list related stuff
        postsList = (ListView) findViewById(R.id.posts_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, postTitles);
        postsList.setAdapter(adapter);
    }

    private void subscribeSingle() {
        Disposable subscription = RXPostConnector.getInstance().getPostsSingle(MainActivity.this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Post>>() {
                    @Override
                    public void accept(List<Post> posts) throws Exception {
                        postTitles.clear();
                        for (Post current : posts) {
                            postTitles.add(current.title);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        disposable.add(subscription);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
