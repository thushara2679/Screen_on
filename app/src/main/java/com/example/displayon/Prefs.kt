package com.example.displayon

import android.content.Context

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("displayon", Context.MODE_PRIVATE)

    /** This phone's own pairing code. */
    fun code(c: Context): String? = sp(c).getString("code", null)
    fun setCode(c: Context, v: String) = sp(c).edit().putString("code", v).apply()

    /** The other phone's pairing code. */
    fun peerCode(c: Context): String = sp(c).getString("peerCode", "") ?: ""
    fun setPeerCode(c: Context, v: String) = sp(c).edit().putString("peerCode", v).apply()
}
