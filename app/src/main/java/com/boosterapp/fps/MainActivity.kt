package com.boosterapp.fps

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.boosterapp.fps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter
    private var gameModeOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apps = BoosterUtils.getInstalledApps(this).toMutableList()
        adapter = AppListAdapter(apps)
        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = adapter

        updateRamDisplay()

        binding.buttonBoost.setOnClickListener {
            runBoost(apps)
        }

        binding.switchGameMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !BoosterUtils.isDndPermissionGranted(this)) {
                Toast.makeText(
                    this,
                    "Game Mode ke liye 'Do Not Disturb' permission allow karein",
                    Toast.LENGTH_LONG
                ).show()
                BoosterUtils.requestDndPermission(this)
                binding.switchGameMode.isChecked = false
                return@setOnCheckedChangeListener
            }
            gameModeOn = isChecked
            BoosterUtils.setGameMode(this, isChecked)
            Toast.makeText(
                this,
                if (isChecked) "Game Mode ON — notifications silent" else "Game Mode OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.buttonShortcutDisplay.setOnClickListener {
            if (!BoosterUtils.openDisplaySettings(this)) {
                Toast.makeText(this, getString(R.string.shortcut_open_failed), Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonShortcutBattery.setOnClickListener {
            if (!BoosterUtils.openBatterySettings(this)) {
                Toast.makeText(this, getString(R.string.shortcut_open_failed), Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonShortcutDeveloper.setOnClickListener {
            if (!BoosterUtils.openDeveloperOptions(this)) {
                Toast.makeText(this, getString(R.string.developer_options_hidden), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun runBoost(apps: List<AppInfo>) {
        val before = BoosterUtils.getMemoryInfo(this).availMem
        val killedCount = BoosterUtils.boostNow(this, apps)

        binding.recyclerApps.postDelayed({
            val after = BoosterUtils.getMemoryInfo(this).availMem
            val freed = (after - before).coerceAtLeast(0)
            updateRamDisplay()
            Toast.makeText(
                this,
                "Boost complete: $killedCount apps band kiye, ~${BoosterUtils.formatBytes(freed)} RAM free hui",
                Toast.LENGTH_LONG
            ).show()
        }, 600)
    }

    private fun updateRamDisplay() {
        val info = BoosterUtils.getMemoryInfo(this)
        val avail = BoosterUtils.formatBytes(info.availMem)
        val total = BoosterUtils.formatBytes(info.totalMem)
        binding.textRamStatus.text = "RAM free: $avail / $total"
        val usedPercent = (100 - (info.availMem * 100 / info.totalMem)).toInt()
        binding.progressRam.progress = usedPercent
    }
}
