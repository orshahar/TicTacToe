package com.yorshahar.tictactoe;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class TicTacToeAdapter extends BaseAdapter {

    public interface Delegate {

        public Integer getLastPositionPlayed();

        public void squareMarked(int position);

        public Square getSquareAtPosition(int position);

        public boolean isPositionNeedsToBeMarkedAsWin(int position);

        public int getNumberOfSquares();

        public boolean isGameOver();

        boolean isBusyPlaying();

    }

    private Delegate delegate;
    private Context context;
    final int xImageId = R.drawable.x;
    final int oImageId = R.drawable.o;
    private final int winBackgroundColor = Color.parseColor("#FFFFCB56");
    private final int squareBackgroundColor = Color.parseColor("#FFFFFFFF");

    public TicTacToeAdapter(Context context) {
        this.context = context;
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getCount() {
        return delegate.getNumberOfSquares();
    }

    @Override
    public Object getItem(int position) {
        return delegate.getSquareAtPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        ImageView squareImageView;

        ViewHolder(View view) {
            squareImageView = (ImageView) view.findViewById(R.id.squareView);
        }
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        View squareView = view;
        final ViewHolder holder;

        if (squareView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            squareView = inflater.inflate(R.layout.single_square, parent, false);
            holder = new ViewHolder(squareView);
            squareView.setTag(holder);

            int squareDimension = ((GridView) parent).getColumnWidth();
            squareView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, squareDimension));

        } else {
            holder = (ViewHolder) squareView.getTag();
        }

        switch (((Square) getItem(position)).getType()) {
            case X: {
                holder.squareImageView.setImageResource(xImageId);
                break;
            }
            case O: {
                holder.squareImageView.setImageResource(oImageId);
                break;
            }
            case EMPTY: {
                holder.squareImageView.setImageDrawable(null);
                // holder.squareImageView.setImageResource(0); // another option
                break;
            }
            default: {
                break;
            }
        }

        if (delegate.isGameOver()) {
            if (delegate.isPositionNeedsToBeMarkedAsWin(position)) {
                holder.squareImageView.setBackgroundColor(winBackgroundColor);
            }
        } else {
            holder.squareImageView.setBackgroundColor(squareBackgroundColor);

            Integer lastPositionPlayed = delegate.getLastPositionPlayed();
            if (lastPositionPlayed != null && position == lastPositionPlayed) {
                holder.squareImageView.setVisibility(View.VISIBLE);
                holder.squareImageView.setAlpha(0.0f);
                holder.squareImageView.setRotation(360.0f);

                holder.squareImageView.animate()
                        .alpha(1.0f)
                        .rotation(0.0f)
                        .setDuration(500)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                delegate.squareMarked(position);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
            }
        }

        return squareView;
    }

    @Override
    public boolean isEnabled(int position) {
        return !delegate.isBusyPlaying() && !delegate.isGameOver();
    }

}
