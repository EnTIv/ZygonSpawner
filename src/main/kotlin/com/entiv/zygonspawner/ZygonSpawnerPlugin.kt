package com.entiv.zygonspawner


import com.entiv.core.common.message.sendMessage
import com.entiv.core.common.plugin.InsekiPlugin
import org.bukkit.Bukkit

class ZygonSpawnerPlugin : InsekiPlugin() {

    override fun onEnabled() {
        val message = arrayOf(
            "&e" + name + "&a 插件&e v" + description.version + " &a已卸载",
            "&a插件制作作者:&e EnTIv &aQQ群:&e 600731934",
            "",
            "&a免费答疑，插件定制，功能请求，合作交流，联系QQ: &e1522935501",
            "&a爱发电主页：&e&nhttps://afdian.net/a/EnTIv"
        )

        Bukkit.getConsoleSender().sendMessage(message)
    }
}