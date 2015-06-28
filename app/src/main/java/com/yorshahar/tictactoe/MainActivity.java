package com.yorshahar.tictactoe;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements TicTacToeAdapter.Delegate {
    private Square[][] squares;
    private int gridSize;
    private GridView board;
    private TicTacToeAdapter adapter;
    private SquareType currentTurn;
    private SquareType winner;
    private Map<String, Integer> occupancy = new HashMap<>();
    private int numOfOccupiedSquares;
    private Integer lastPositionPlayed;
    private boolean busyPlaying;
    private Integer winningRow;
    private Integer winningColumn;
    private boolean winningDiagonalRight;
    private boolean winningDiagonalLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridSize = getResources().getInteger(R.integer.grid_size);
        gridSize = getResources().getInteger(R.integer.grid_size);
        squares = new Square[gridSize][gridSize];
        board = (GridView) findViewById(R.id.gridView);
        adapter = new TicTacToeAdapter(this);
        adapter.setDelegate(this);
        board.setAdapter(adapter);

        initBoard();
    }

    private void initBoard() {
        board.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!busyPlaying) {
                    busyPlaying = true;
                    play(position);
                }
            }
        });

        for (int row = 0; row < gridSize; row++) {
            for (int column = 0; column < gridSize; column++) {
                squares[row][column] = new Square(SquareType.EMPTY);
            }
        }

        currentTurn = SquareType.X;
        winner = null;
        winningRow = null;
        winningColumn = null;
        winningDiagonalLeft = false;
        winningDiagonalRight = false;
        occupancy.clear();
        numOfOccupiedSquares = 0;
        lastPositionPlayed = null;
        busyPlaying = false;
        board.invalidateViews();
//        adapter.notifyDataSetChanged();
    }



    private void resetBoard() {
        for (int row = 0; row < gridSize; row++) {
            for (int column = 0; column < gridSize; column++) {
                squares[row][column].setType(SquareType.EMPTY);
            }
        }

        currentTurn = SquareType.X;
        winner = null;
        winningRow = null;
        winningColumn = null;
        winningDiagonalLeft = false;
        winningDiagonalRight = false;
        occupancy.clear();
        numOfOccupiedSquares = 0;
        lastPositionPlayed = null;
        busyPlaying = false;
        board.invalidateViews();
//        adapter.notifyDataSetChanged();
    }

    private void saveStatus() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.num_of_occupied_squares), numOfOccupiedSquares);
        editor.apply();
    }

    private void readStatus() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
//        int defaultValue = getResources().getInteger(R.string.num_of_occupied_squares_default);
        int numOfOccupiedSquares = sharedPref.getInt(getString(R.string.num_of_occupied_squares), 0);
    }

    private void endGame() {
        finish();
        System.exit(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readStatus();
    }

    private void play(int position) {
        int row = position / gridSize;
        int column = position % gridSize;

        Square square = squares[row][column];
        if (square.getType() == SquareType.EMPTY) {
            square.setType(currentTurn);
            numOfOccupiedSquares++;
            lastPositionPlayed = position;

            // Update row status
            String occupancyKey = currentTurn.name() + "row" + row;
            Integer playerRowOccupancy = occupancy.get(occupancyKey);
            if (playerRowOccupancy == null) {
                playerRowOccupancy = 0;
            }
            playerRowOccupancy++;
            occupancy.put(occupancyKey, playerRowOccupancy);

            // Update column status
            occupancyKey = currentTurn.name() + "col" + column;
            Integer playerColumnOccupancy = occupancy.get(occupancyKey);
            if (playerColumnOccupancy == null) {
                playerColumnOccupancy = 0;
            }
            playerColumnOccupancy++;
            occupancy.put(occupancyKey, playerColumnOccupancy);

            // Update diagonal right status
            if (row == column) {
                occupancyKey = currentTurn.name() + "diagRight";
                Integer playerDiagonalRightOccupancy = occupancy.get(occupancyKey);
                if (playerDiagonalRightOccupancy == null) {
                    playerDiagonalRightOccupancy = 0;
                }
                playerDiagonalRightOccupancy++;
                occupancy.put(occupancyKey, playerDiagonalRightOccupancy);
            }

            // Update diagonal left status
            if (row + column == gridSize - 1) {
                occupancyKey = currentTurn.name() + "diagLeft";
                Integer playerDiagonalLeftOccupancy = occupancy.get(occupancyKey);
                if (playerDiagonalLeftOccupancy == null) {
                    playerDiagonalLeftOccupancy = 0;
                }
                playerDiagonalLeftOccupancy++;
                occupancy.put(occupancyKey, playerDiagonalLeftOccupancy);
            }

            adapter.notifyDataSetChanged();
        }
    }

    private void turnOver() {
        checkWin();

        if (isGameOver()) {
            adapter.notifyDataSetChanged();
            announceWinner();
        } else {
            currentTurn = currentTurn == SquareType.X ? SquareType.O : SquareType.X;
        }

        busyPlaying = false;
    }

    private void checkWin() {
        int row = lastPositionPlayed / gridSize;
        int column = lastPositionPlayed % gridSize;

        // Check row
        String occupancyKey = currentTurn.name() + "row" + row;
        Integer playerRowOccupancy = occupancy.get(occupancyKey);
        if (playerRowOccupancy != null && playerRowOccupancy == gridSize) {
            winner = currentTurn;
            winningRow = row;
        }

        // Check column
        occupancyKey = currentTurn.name() + "col" + column;
        Integer playerColumnOccupancy = occupancy.get(occupancyKey);
        if (playerColumnOccupancy != null && playerColumnOccupancy == gridSize) {
            winner = currentTurn;
            winningColumn = column;
        }

        // Check diagonal right
        occupancyKey = currentTurn.name() + "diagRight";
        Integer playerDiagonalRightOccupancy = occupancy.get(occupancyKey);
        if (playerDiagonalRightOccupancy != null && playerDiagonalRightOccupancy == gridSize) {
            winner = currentTurn;
            winningDiagonalRight = true;
        }

        // Check diagonal left
        occupancyKey = currentTurn.name() + "diagLeft";
        Integer playerDiagonalLeftOccupancy = occupancy.get(occupancyKey);
        if (playerDiagonalLeftOccupancy != null && playerDiagonalLeftOccupancy == gridSize) {
            winner = currentTurn;
            winningDiagonalLeft = true;
        }
    }

    public boolean isGameOver() {
        return winner != null || numOfOccupiedSquares == gridSize * gridSize;
    }

    private void announceWinner() {
        Dialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                .setMessage(winner != null ? winner.name() + " wins!\n\nPlay again?" : "Game over.\n\nPlay again?")
                .setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetBoard();
                    }
                })
                .setNegativeButton(R.string.end_game, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        endGame();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).show();

        dialog.getWindow().getDecorView().setAlpha(0.9f);
    }

    //////////////////////////////////
    /* Action Bar */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu items for use in the action bar
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View settingsView = inflater.inflate(R.layout.settings, null);
        TextView gridSizeTextView = (TextView) settingsView.findViewById(R.id.gridSizeTextView);
        gridSizeTextView.setText(String.valueOf(gridSize));
//        setContentView(settingsView);

        Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
        intent.putExtra("gridSize", gridSize);
        startActivity(intent);
    }

    //////////////////////////////////
    /* Tic Tac Toe Delegate methods */

    @Override
    public Integer getLastPositionPlayed() {
        return lastPositionPlayed;
    }

    @Override
    public void squareMarked(int position) {
//        board.getAdapter().areAllItemsEnabled();
        turnOver();
    }

    @Override
    public Square getSquareAtPosition(int position) {
        int row = position / gridSize;
        int column = position % gridSize;
        return squares[row][column];
    }

    @Override
    public boolean isPositionNeedsToBeMarkedAsWin(int position) {
        int row = position / gridSize;
        int column = position % gridSize;

        // Check row
        if (winningRow != null && row == winningRow) {
            return true;
        }

        // Check column
        if (winningColumn != null && column == winningColumn) {
            return true;
        }

        // Check diagonal right
        if (winningDiagonalRight && row == column) {
            return true;
        }

        // Check diagonal right
        return (winningDiagonalLeft && row + column == gridSize - 1);
    }

    @Override
    public int getNumberOfSquares() {
        return gridSize * gridSize;
    }

    @Override
    public boolean isBusyPlaying() {
        return busyPlaying;
    }

}

