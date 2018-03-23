package com.example.ravisharma.musicdemo1;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecentAdded.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecentAdded#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecentAdded extends Fragment implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {
    FastScrollRecyclerView recyclerView;
    SongAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Song> songList;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RecentAdded() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecentAdded.
     */
    // TODO: Rename and change types and number of parameters
    public static RecentAdded newInstance(String param1, String param2) {
        RecentAdded fragment = new RecentAdded();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_recent_added, container, false);
        songList = new ArrayList<Song>();
        getSongList();
        recyclerView = v.findViewById(R.id.song_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return b.getDateModified().compareToIgnoreCase(a.getDateModified());
            }
        });
        if(MainActivity.getInstance().lastSongId!=null && MainActivity.getInstance().fromRecent){
            MainActivity.getInstance().setStartingList(songList);
        }
        adapter = new SongAdapter(songList);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
        adapter.setOnLongClick(this);
        return v;
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContext().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null,
                MediaStore.Audio.Media.DURATION + ">= 34000", null,null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns

            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int dateModifyColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisData = musicCursor.getString(dataColumn);
                String thisDateModify = musicCursor.getString(dateModifyColumn);
                long thisDuration = musicCursor.getLong(durationColumn);
                long thisAlbumAid = musicCursor.getLong(albumIdColumn);
                final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                Uri albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisData, thisDateModify, albumArt, thisDuration));
            }
            while (musicCursor.moveToNext());
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(int position) {
        OnFragmentItemClicked onFragmentItemClicked = (OnFragmentItemClicked)getActivity();
        onFragmentItemClicked.OnFragmentItemClick(position, songList);
        MainActivity.getInstance().rcnt(true);
    }

    @Override
    public void onItemLongClick(int position) {
        OnFragmentItemLongClicked onFragmentItemLongClicked = (OnFragmentItemLongClicked)getActivity();
        onFragmentItemLongClicked.OnFragmentItemLongClick(position);
    }

    public interface OnFragmentItemLongClicked {
        void OnFragmentItemLongClick(int position);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface OnFragmentItemClicked {
        void OnFragmentItemClick(int position, ArrayList<Song> songsArrayList);
    }
}
