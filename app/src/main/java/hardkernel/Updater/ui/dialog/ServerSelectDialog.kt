package hardkernel.Updater.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import hardkernel.Updater.R
import hardkernel.Updater.Logic.ServerManager
import hardkernel.Updater.Logic.ServerManager.Server

class ServerSelectDialog(context: Context): AlertDialog(context), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_select_dailog)
        val official: RadioButton = findViewById(R.id.official_btn)
        val mirror: RadioButton = findViewById(R.id.mirror_btn)
        val custom: RadioButton = findViewById(R.id.custom_btn)
        official.setOnClickListener(this)
        mirror.setOnClickListener(this)
        custom.setOnClickListener(this)

        when (ServerManager.getCurrent()) {
            Server.Official -> official.isChecked = true
            Server.Mirror -> mirror.isChecked = true
            Server.Custom -> custom.isChecked = true
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.official_btn -> {
                ServerManager.setOfficial()
                this.dismiss()
            }
            R.id.mirror_btn -> {
                ServerManager.setMirror()
                this.dismiss()
            }
            R.id.custom_btn -> {
                val builder = Builder(context)
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                if (ServerManager.getCustomURL() != "")
                    input.setText(ServerManager.getCustomURL())
                else
                    input.hint = "Enter Custom URL"
                builder.setView(input)

                builder.setPositiveButton("OK") { _, _ ->
                    ServerManager.setCustom(input.text.toString())
                    this.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                builder.show()
            }
        }
    }
}
