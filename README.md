# RocketUp

![Lines of code](https://img.shields.io/tokei/lines/github/YertinMC/RocketUp?style=flat-square)![GitHub all releases](https://img.shields.io/github/downloads/YertinMC/RocketUp/total?style=flat-square)![GitHub issues](https://img.shields.io/github/issues/YertinMC/RocketUp?style=flat-square)![GitHub pull requests](https://img.shields.io/github/issues-pr/YertinMC/RocketUp?style=flat-square)![GitHub](https://img.shields.io/github/license/YertinMC/RocketUp?style=flat-square)![GitHub forks](https://img.shields.io/github/forks/YertinMC/RocketUp?style=flat-square)![GitHub Repo stars](https://img.shields.io/github/stars/YertinMC/RocketUp?style=flat-square)![GitHub watchers](https://img.shields.io/github/watchers/YertinMC/RocketUp?style=flat-square)![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/YertinMC/RocketUp?include_prereleases&style=flat-square)

## 这是什么

**超级烟花火箭**Bukkit插件

## 这玩意有什么用

可以做增强的烟花火箭，右键可以起飞然后滑翔

<img src="https://static.wikia.nocookie.net/minecraft_zh_gamepedia/images/6/6f/Elytra_JE2_BE2.png/revision/latest/scale-to-width-down/150?cb=20200612161322" alt="Elytra JE2 BE2.png" style="zoom: 20%;" />：（怒）

不仅可以自定义物品，还能添加粒子效果，还能设置起飞时的弹射角度

## 怎么用

理论上Bukkit系服务器全版本支持（没有NMS）

在[Releases](https://github.com/YertinMC/RocketUp/releases)可以下载正式版，[Actions](https://github.com/YertinMC/RocketUp/actions)有开发版

记得看默认配置，里面有很详细的文档，默认配置测试指令：

`/give @s minecraft:firework_rocket{display:{Lore:["\"超级烟花火箭#default\""]}} 128`

## 注意

- **请勿让玩家在起飞过程中退出游戏或在玩家起飞时重载服务器或突然关闭服务器，否则玩家会进入`NoGravity`（失重状态，但不会继续向上飞）**，再次使用超级烟火即可解决（也可以手动移除`NoGravity`的NBT）

## 协议

```
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```



