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
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
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
                downloadPosts();
            }
        });

        // list related stuff
        postsList = (ListView) findViewById(R.id.posts_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, postTitles);
        postsList.setAdapter(adapter);
    }

    private void downloadPosts() {
        Disposable subscription = RXPostConnector.getInstance().getPosts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(getObserver());

        // add the subscription to the list to avoid a possible leak of references
        disposable.add(subscription);
    }

    private DisposableSingleObserver<List<Post>> getObserver(){
        return new DisposableSingleObserver<List<Post>>() {
            @Override
            public void onSuccess(@NonNull List<Post> posts) {
                postTitles.clear();
                for (Post current : posts) {
                    postTitles.add(current.title);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
