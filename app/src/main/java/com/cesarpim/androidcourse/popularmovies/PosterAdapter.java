package com.cesarpim.androidcourse.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by CesarPim on 03-02-2017.
 *
 * Adapter for the RecyclerView of movie posters.
 *
 * @author CesarPim
 */

class PostersAdapter extends RecyclerView.Adapter<PostersAdapter.PosterViewHolder> {

    private final PosterClickListener clickListener;
    private Movie[] movies;
    private Context context;

    public PostersAdapter(Movie[] movies, Context context, PosterClickListener clickListener) {
        this.movies = movies;
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public PosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_item, parent, false);
        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PosterViewHolder holder, int position) {
        String posterStringURL =
                context.getString(R.string.themoviedb_image_base_url)
                        + context.getString(R.string.themoviedb_image_size)
                        + movies[position].getPosterPath();
        Picasso.with(context).load(posterStringURL).into(holder.poster);
    }

    @Override
    public int getItemCount() {
        return movies.length;
    }

    public void updateMovies(Movie[] movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public interface PosterClickListener {
        void onPosterClick(int clickedPosterIndex);
    }

    class PosterViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView poster;

        public PosterViewHolder(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.image_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            clickListener.onPosterClick(clickedPosition);
        }
    }
}
