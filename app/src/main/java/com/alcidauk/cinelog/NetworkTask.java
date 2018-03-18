package com.alcidauk.cinelog;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;

import com.alcidauk.cinelog.dao.LocalKino;
import com.alcidauk.cinelog.dao.TmdbKino;
import com.uwetrottmann.tmdb2.entities.Movie;
import com.uwetrottmann.tmdb2.entities.MovieResultsPage;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.List;

import retrofit2.Call;

public class NetworkTask extends AsyncTask<Call<MovieResultsPage>, Void, List<Movie>> {

    private AddKino addKino;

    public NetworkTask(AddKino addKino) {
        this.addKino = addKino;
    }

    protected List<Movie> doInBackground(Call<MovieResultsPage>... results) {
        List<Movie> movies = null;
        try {
            if (!isCancelled()) {
                movies = results[0].execute().body().results;
            }
        } catch (java.io.IOException e) {
            return null;
        }
        return movies;
    }

    protected void onPostExecute(List<Movie> movies) {
        if (!isCancelled()) {
            populateListView(movies);
        }
    }

    private void populateListView(final List<Movie> movies) {
        if (addKino.kino_results_list != null) {
            addKino.kino_results_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> view, View parent, final int position, long rowId) {
                    Intent intent = new Intent(view.getContext(), ViewKino.class);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    String year = "";
                    int year_i = 0;
                    if (movies.get(position).release_date != null) {
                        year = sdf.format(movies.get(position).release_date);
                        year_i = Integer.parseInt(year);
                    }

                    System.out.println(year_i);

                    TmdbKino tmdbKino = new TmdbKino(
                            movies.get(position).id.longValue(),
                            movies.get(position).poster_path,
                            movies.get(position).overview,
                            year_i,
                            year
                    );
                    LocalKino kino = new LocalKino(movies.get(position).title, tmdbKino);
                    intent.putExtra("kino", Parcels.wrap(kino));
                    addKino.startActivity(intent);
                }
            });

            addKino.kino_results_list.setAdapter(new KinoResultsAdapter(addKino, movies));
            addKino.kino_search_progress_bar.setVisibility(View.GONE);
        }
    }
}
