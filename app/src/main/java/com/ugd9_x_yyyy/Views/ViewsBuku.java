package com.ugd9_x_yyyy.Views;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ugd9_x_yyyy.API.BukuAPI;
import com.ugd9_x_yyyy.API.MahasiswaAPI;
import com.ugd9_x_yyyy.Adapters.AdapterBuku;
import com.ugd9_x_yyyy.Adapters.AdapterMahasiswa;
import com.ugd9_x_yyyy.Models.Buku;
import com.ugd9_x_yyyy.Models.Mahasiswa;
import com.ugd9_x_yyyy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.Method.GET;

public class ViewsBuku extends Fragment{

    private RecyclerView recyclerView;
    private AdapterBuku adapter;
    private List<Buku> listBuku;
    private View view;
    private int orientation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_views_buku, container, false);

        loadDaftarBuku();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bar_buku, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.btnSearch);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.btnSearch).setVisible(true);
        menu.findItem(R.id.btnAdd).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.btnAdd) {
            Bundle data = new Bundle();
            data.putString("status", "tambah");
            TambahEditBuku tambahEditBuku = new TambahEditBuku();
            tambahEditBuku.setArguments(data);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager .beginTransaction()
                    .replace(R.id.frame_view_buku, tambahEditBuku)
                    .commit();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadDaftarBuku(){
        setAdapter();
        getBuku();
    }

    public void setAdapter(){
        getActivity().setTitle("Data Buku");
        /*Buat tampilan untuk adapter jika potrait menampilkan 2 data dalam 1 baris,
        sedangakan untuk landscape 4 data dalam 1 baris*/
        orientation = getActivity().getResources().getConfiguration().orientation;
        listBuku = new ArrayList<Buku>();
        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new AdapterBuku(view.getContext(), listBuku, new AdapterBuku.deleteItemListener() {
            @Override
            public void deleteItem(Boolean delete) {
                if(delete){
                    loadDaftarBuku();
                }
            }
        });
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 2
                    , GridLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(gridLayoutManager);
        }else   {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 4
                    , GridLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


    }

    public void getBuku() {
        //Tambahkan tampil buku disini
        //Pendeklarasian queue
        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        //Meminta tanggapan string dari URL yang telah disediakan menggunakan method GET
        //untuk request ini tidak memerlukan parameter
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("loading....");
        progressDialog.setTitle("Menampilkan data buku");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        final JsonObjectRequest stringRequest = new JsonObjectRequest(GET, BukuAPI.URL_SELECT_BUKU
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                progressDialog.dismiss();
                try {
                    //Mengambil data response json object yang berupa data mahasiswa
                    JSONArray jsonArray = response.getJSONArray("dataBuku");

                    if(!listBuku.isEmpty())
                        listBuku.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        //Mengubah data jsonArray tertentu menjadi json Object
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                        int idBuku              = jsonObject.optInt("idBuku");
                        String namaBuku         = jsonObject.optString("namaBuku");
                        String pengarang        = jsonObject.optString("pengarang");
                        Double harga            = jsonObject.optDouble("harga");
                        String gambar           = jsonObject.optString("gambar");

                        //Membuat objek buku
                        Buku buku = new Buku(idBuku, namaBuku, pengarang, harga, gambar);

                        //Menambahkan objek buku tadi ke list buku
                        listBuku.add(buku);
                    }
                    adapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Toast.makeText(view.getContext(), response.optString("message"),
                        Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                progressDialog.dismiss();
                Toast.makeText(view.getContext(), error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        //Disini proses penambahan request yang sudah kita buat ke reuest queue yang sudah dideklarasi
        queue.add(stringRequest);
    }
}