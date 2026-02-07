package com.example.northfutbol

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class LoginDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_login, container, false)

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val txtSwitchRegister = view.findViewById<TextView>(R.id.txtSwitchRegister)

        btnLogin.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.inputEmail).text.toString()
            val password = view.findViewById<EditText>(R.id.inputPassword).text.toString()

            // TODO: aquí va la validación del login (backend o Firebase)

            dismiss() // cerrar modal
        }

        //txtSwitchRegister.setOnClickListener {
        //    val registerDialog = RegisterDialogFragment()
        //    registerDialog.show(parentFragmentManager, "registerDialog")
        //    dismiss()
        //}

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}