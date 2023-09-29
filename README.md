# PojavLauncher-非官方汉化更新
<img src="https://github.com/HopiHopy/PojavZH/blob/v3_openjdk/PojavZH.png" align="right" width="264" height="264" alt="PojavZH logo">

[![Android CI](https://github.com/HopiHopy/PojavZH/actions/workflows/android.yml/badge.svg)](https://github.com/HopiHopy/PojavZH/actions/workflows/android.yml)
## 说明
* 您好！欢迎来到PojavLauncher非官方汉化更新的分支！在这里，您能够获取到完全汉化后的PojavLauncher！  
* 支持简体中文与繁體中文（可能不算標準，如果您認為質量有問題，還請在[B站](https://space.bilibili.com/2008204513)私信告訴我哦）  
* **如果有翻译上的错误，欢迎在[B站](https://space.bilibili.com/2008204513)私信告诉我！**
***
* 我会与官方PojavLauncher主线[v3_openjdk](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk)保持同步更新（稍晚）。  
* **只会同步v3_openjdk主线，其他任何支线均不同步**  
* 中文语言文件大部分来自官方[Crowdin](https://crowdin.com/project/pojavlauncher/zh-ZH)，点击查看翻译进度！不过后续的相关翻译，我便不再参考官方Crowdin，全部由我独立翻译！ 
## 支线
* **PojavZH是有不同的分支的：**  
  - v3_openjdk：这条支线是默认支线，也是直接改自官方PojavLauncher的主线[v3_openjdk](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk)。当然，版本号也会跟随官方PojavLauncher更新，保证不会出现安装失败的问题（例如版本号过低、签名不一致、更新完PojavZH后无法再次回退安装官方PojavLauncher）
    
  - Alone：这条支线虽然内容与v3_openjdk支线保持一致，但是他的软件包名从<kbd>net.kdt.pojavlaunch.debug</kbd>改为了<kbd>net.kdt.pojavlaunch.CN.debug</kbd>，这样一来，就可以实现与官方PojavLauncher共存了
    
  - _以及其他不重要的支线，不用在意他们_

## 构建
### 本地构建
> * 因为语言是由Crowdin自动添加的，所以您需要在构建之前运行语言文件生成器。在项目目录中运行：  
> * Linux、Mac OS：  
> ```
> chmod +x scripts/languagelist_updater.sh
> bash scripts/languagelist_updater.sh
> ```
> * Windows：  
> ```
> scripts\languagelist_updater.bat
> ```
> * 然后，运行这些命令：
> * 构建GLFW：  
> ```
> ./gradlew :jre_lwjgl3glfw:build
> ```       
> * 构建启动器：  
> ```
> ./gradlew :app_pojavlauncher:assembleDebug
> ```
> * (如果在Windows上构建，请将“gradlew”替换为“gradlew.bat”)
### 在Github构建
> * 建议您直接将此代码库中的[工作流文件](https://github.com/HopiHopy/PojavZH/blob/v3_openjdk/.github/workflows/android.yml)复制到您自己的仓库中！  
> * 如果您需要对工作流文件进行修改，那么您需要注意下面的内容
> * 给予语言文件生成器执行权限：  
> ```
> - name: chmod +x languagelist_updater.sh
>   run: chmod +x ./scripts/languagelist_updater.sh
> ```
> * 然后再运行语言文件生成器：  
> ```
> - name: run languagelist_updater.sh
>   run: |
>     ./scripts/languagelist_updater.sh
> ```
> * （否则您在构建代码时可能会因权限不足而构建失败）

## 下载
- 前往[Actions](https://github.com/HopiHopy/PojavZH/actions)查看最新的中文语言更新版本(主线：v3_openjdk、独立软件：Alone，其他任何支线均可忽略！)
，进入后，点击**Pojav汉化更新**下载。

- 前往[Release](https://github.com/HopiHopy/PojavZH/releases)查看最新且经过测试的稳定版本。
