package angel.restconnections.net;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import angel.restconnections.MainActivity;
import angel.restconnections.domain.Post;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Created by angel on 8/19/2017.
 */

public class RXPostConnector {

    private static final String ENDPOINT_GET_POSTS = "https://jsonplaceholder.typicode.com/posts";

    private static RXPostConnector instance = new RXPostConnector();

    public static RXPostConnector getInstance() {
        return instance;
    }

    private RXPostConnector() {
    }


    private Post getPost(JSONObject current) throws JSONException {
        Post result = new Post();
        result.id = current.getInt("id");
        result.userId = current.getInt("userId");
        result.body = current.getString("body");
        result.title = current.getString("title");
        return result;
    }

    public Single<List<Post>> getPosts() {
        return Single.create(new SingleOnSubscribe<List<Post>>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<List<Post>> e) throws Exception {
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, ENDPOINT_GET_POSTS, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response != null) {
                                    ArrayList<Post> result = new ArrayList<>();

                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            result.add(getPost(response.getJSONObject(i)));
                                        }
                                    } catch (JSONException ex) {
                                        e.onError(ex);
                                    }

                                    e.onSuccess(result);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                e.onError(error);
                            }
                        }
                );

                VolleyDispatcher.getInstance().addToQueue(jsonObjectRequest);
            }
        });
    }
}
