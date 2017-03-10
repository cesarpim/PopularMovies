package com.cesarpim.androidcourse.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by CesarPim on 09-03-2017.
 */

class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private final TrailerClickListener clickListener;
    private Trailer[] trailers;

    public TrailersAdapter(Trailer[] trailers, TrailerClickListener clickListener) {
        this.trailers = trailers;
        this.clickListener = clickListener;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Trailer trailer = trailers[position];
        holder.trailerTitle.setText(trailer.getTitle());
        holder.trailerTitle.setTag(trailer.getYoutubeKey());
    }

    @Override
    public int getItemCount() {
        return trailers.length;
    }

    public void updateTrailers(Trailer[] trailers) {
        this.trailers = trailers;
        notifyDataSetChanged();
    }

    public interface TrailerClickListener {
        void onTrailerClick(String clickedTrailerYoutubeKey);
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView trailerTitle;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            trailerTitle = (TextView) itemView.findViewById(R.id.text_trailer_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onTrailerClick((String) view.getTag());
        }
    }

}
