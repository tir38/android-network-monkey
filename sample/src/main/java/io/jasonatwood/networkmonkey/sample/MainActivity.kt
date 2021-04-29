package io.jasonatwood.networkmonkey.sample

import android.app.Activity
import android.os.Bundle
import io.jasonatwood.networkmonkey.LiveNetworkMonkey

class MainActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val networkMonkey = LiveNetworkMonkey(this)
    }

}