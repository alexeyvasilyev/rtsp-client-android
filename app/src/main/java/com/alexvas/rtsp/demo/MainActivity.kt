package com.alexvas.rtsp.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.petrocik.onvif.wcf.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sentence = object : UIIIServiceEvents {
            override fun Starting() {};
            override fun Completed(result: UIIOperationResult<*>?) {
                val res: UIIGetDeviceInformationResponse = result!!.Result as ObjectWithListForRPC
            };
        }

        var service = UIIDeviceBinding(sentence, "");
        service.GetDeviceInformationAsync();

//        val navView: BottomNavigationView = findViewById(R.id.nav_view)

//        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(setOf(
//                R.id.navigation_live, R.id.navigation_logs))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }
}
