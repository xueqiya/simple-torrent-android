package com.masterwok.demosimpletorrentandroid.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import com.masterwok.demosimpletorrentandroid.R
import com.masterwok.demosimpletorrentandroid.adapters.TabFragmentPagerAdapter
import com.masterwok.demosimpletorrentandroid.fragments.TorrentFragment
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.extensions.appCompatRequestPermissions
import com.masterwok.simpletorrentandroid.extensions.isPermissionGranted
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import com.nononsenseapps.filepicker.FilePickerActivity


/**
 * This activity is responsible for creating and presenting multiple torrent fragments.
 * The [@see TorrentSession] and its associated [@see TorrentSessionListener] exist within
 * [@see TorrentFragment]. In the real world, you would want to have the
 * [@see TorrentSession] live in a foreground service so its not interrupted when the device
 * goes to sleep.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var buttonAddTorrent: AppCompatButton

    companion object {
        const val FilePickerRequestCode = 6906
    }

    private val torrentUrls = arrayOf(
        "https://www.frostclick.com/torrents/audio/music/Aint_No_Love_Tears_Of_Joy_FrostClick_FrostWire_MP3_320k_Mar_19_2014.torrent",
        "magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Sintel&tr=udp%3A%2F%2Fexplodie.org%3A6969&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Ftracker.empire-js.us%3A1337&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337&tr=wss%3A%2F%2Ftracker.btorrent.xyz&tr=wss%3A%2F%2Ftracker.fastcast.nz&tr=wss%3A%2F%2Ftracker.openwebtorrent.com&ws=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2F&xs=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2Fsintel.torrent",
        "magnet:?xt=urn:btih:581baa2d4772fb1f1738d4321644cdfb02b78fd8&dn=%e9%98%b3%e5%85%89%e7%94%b5%e5%bd%b1www.ygdy8.com.%e9%80%9f%e5%ba%a6%e4%b8%8e%e6%bf%80%e6%83%859.2021.1080P.%e5%9b%bd%e8%8b%b1%e5%8f%8c%e8%af%ad%e5%8f%8c%e5%ad%97.mkv&tr=udp%3a%2f%2ftracker.opentrackr.org%3a1337%2fannounce&tr=udp%3a%2f%2fexodus.desync.com%3a6969%2fannounce"
    )

    private val torrentSessionOptions = TorrentSessionOptions(
            downloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            , onlyDownloadLargestFile = true
            , enableLogging = true
            , shouldStream = true
    )

    private val torrentSessionPagerAdapter = TabFragmentPagerAdapter<TorrentFragment, TorrentSessionStatus>(
            supportFragmentManager
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViewComponents()
        subscribeToViewComponents()
        initTabLayout()

        if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            appCompatRequestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    , 0
            )

            return
        }
    }

    private fun initTabLayout() {
        viewPager.adapter = torrentSessionPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun subscribeToViewComponents() {
        buttonAddTorrent.setOnClickListener {
            // startFilePickerActivity() // Be sure to comment the two lines below if you uncomment this.
            val tabFragment = createTabWithUri(Uri.parse(torrentUrls[torrentSessionPagerAdapter.count]))
            torrentSessionPagerAdapter.addTab(tabFragment)

            if (torrentSessionPagerAdapter.count == torrentUrls.size) {
                buttonAddTorrent.apply {
                    text = context.getString(R.string.button_all_torrents_added)
                    isEnabled = false
                }
            }
        }
    }

    private fun createTabWithUri(torrentUri: Uri): TorrentFragment =
            TorrentFragment.newInstance(
                    this
                    , torrentSessionPagerAdapter.count + 1
                    , torrentUri
                    , torrentSessionOptions
            )

    private fun bindViewComponents() {
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        buttonAddTorrent = findViewById(R.id.button_add_torrent)
    }

    /**
     * This method can be invoked in the buttonAddTorrent click listener to select
     * a torrent file.
     */
    @Suppress("unused")
    private fun startFilePickerActivity() {
        val intent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
            }
        } else {
            // Fallback to external file picker.
            intent = Intent(this, FilePickerActivity::class.java).apply {
                putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE)
                putExtra(FilePickerActivity.EXTRA_START_PATH, torrentSessionOptions.downloadLocation)
            }
        }

        startActivityForResult(intent, FilePickerRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FilePickerRequestCode
                || resultCode != Activity.RESULT_OK
                || intent == null) {
            return
        }

        val tabFragment = createTabWithUri(intent.data)

        torrentSessionPagerAdapter.addTab(tabFragment)
    }


}
