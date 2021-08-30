package hardkernel.Updater.ui.main

import hardkernel.Updater.logic.Loader

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hardkernel.Updater.R
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import android.app.Activity.RESULT_OK
import java.io.IOException
import android.util.Log

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var viewOfLayer: View

    private lateinit var loader: Loader

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewOfLayer = inflater.inflate(R.layout.main_fragment, container, false)

        loader = Loader(getContext())
        val btn_update_from_local: Button = viewOfLayer.findViewById(R.id.btn_update_from_local);
        btn_update_from_local.setOnClickListener {
            val intent: Intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "application/zip"
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, 100)
        }

        return viewOfLayer;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                100 -> {
                    try {
                        loader.updateFromLocal(data!!.getData())
                    } catch(e: IOException) {
                        Toast.makeText(getContext(), "Error" + e,
                            Toast.LENGTH_LONG).show()
                            Log.d("Updater", "Error" + e)
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
