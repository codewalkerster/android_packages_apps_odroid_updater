package hardkernel.Updater.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import hardkernel.Updater.R
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import android.app.Activity.RESULT_OK
import java.io.IOException
import android.util.Log
import android.view.*
import android.widget.TextView
import hardkernel.Updater.Logic.ServerManager
import hardkernel.Updater.Service.UpdateService
import hardkernel.Updater.ui.dialog.ServerSelectDialog

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var viewOfLayer: View

    private lateinit var currentServer: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewOfLayer = inflater.inflate(R.layout.main_fragment, container, false)

        val btn_update_from_local: Button = viewOfLayer.findViewById(R.id.btn_update_from_local)
        btn_update_from_local.setOnClickListener {
            val intent: Intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "application/zip"
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, 100)
        }

        val btn_update_from_online: Button = viewOfLayer.findViewById(R.id.btn_update_from_online)
        btn_update_from_online.setOnClickListener {
            val intent = Intent(context, UpdateService::class.java)
            intent.action = UpdateService.CMD.REMOTE_UPDATE_START.name
            intent.putExtra(UpdateService.CMD.REMOTE_UPDATE_START.name, ServerManager.getURL())
            context?.startService(intent)
        }
        currentServer = viewOfLayer.findViewById(R.id.currentServer)
        currentServer.text = ServerManager.getCurrent().name

        val btn_select_update_server: Button = viewOfLayer.findViewById(R.id.btn_select_update_server)
        btn_select_update_server.setOnClickListener {
            val dialog = ServerSelectDialog(this.requireContext())
            dialog.show()
        }

        return viewOfLayer
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                100 -> {
                    try {
                        val intent = Intent(context, UpdateService::class.java)
                        intent.action = UpdateService.CMD.LOCAL_UPDATE_START.name
                        intent.putExtra(UpdateService.CMD.LOCAL_UPDATE_START.name, data?.data)
                        context?.startService(intent)
                    } catch(e: IOException) {
                        Toast.makeText(
                            context, "Error $e",
                            Toast.LENGTH_LONG).show()
                            Log.d("Updater", "Error, Try with direct selection, $e")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentServer.text = ServerManager.getCurrent().name
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
