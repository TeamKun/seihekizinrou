<h1 align="center">性癖人狼</h1>

<p align="center"><a href="https://twitter.com/silver_whale_">銀色くじら</a>さん考案の<a href="https://twitter.com/silver_whale_/status/971322036567072778">性癖人狼</a>をマインクラフトで遊べるようにしたプラグインです。</p>

<div align="center">
    <a href="https://github.com/TeamKun/seihekizinrou"><img src="https://img.shields.io/github/workflow/status/TeamKun/seihekizinrou/Build?style=flat-square" alt="Build Result"></a>
    <a href="https://github.com/TeamKun/seihekizinrou"><img src="https://img.shields.io/github/v/release/TeamKun/seihekizinrou?color=blueviolet&label=version&style=flat-square" alt="latest release version"></a>
    <a href="https://opensource.org/licenses/mit-license.php"><img src="https://img.shields.io/static/v1?label=license&message=MIT&style=flat-square&color=blue" alt="License"></a>
    <a href="https://twitter.com/kotx__"><img src="https://img.shields.io/static/v1?label=developer&message=kotx__&style=flat-square&color=orange" alt="developer"></a>
</div>


## 🎮 How to play
1. `/szinrou start` でゲームを始めます。参加するプレイヤーは自身の性癖をチャットに入力します。
2. 90秒が経過するか、全員が自身の性癖を入力し終えると、"人狼"となるプレイヤーがランダムで選ばれ、その性癖が公表されます。(人狼以外の参加プレイヤーは"村人"となります。)
3. 参加するプレイヤーは、公表された性癖を元に人狼が誰であるかを推理し、処刑する必要があります。
4. 人狼は毎日夜間に１人、村人を襲うことができます。襲われた村人は次の日の朝に性癖を暴露され、ゲームからは除外されます。
5. 村人が公表された性癖を持つ人狼を処刑するか、生き残っている村人より人狼が多くなった場合にゲームは終了となります。

一日のサイクルは以下の通りです。
```
朝: 襲われた村人の性癖が暴露される。 (2日目の朝から)
昼: 公表された性癖を持つ人狼をプレイヤーは推測(120秒間)、人狼を１人処刑する。
　  人狼は自身がその性癖の持ち主であるとバレないように話を逸らしたりして立ち回る。
晩: 人狼は性癖を暴露したい村人を１人襲う。
```

## ⚙️ Commands
`/szinrou start` ゲームを始めます。ゲームは自動的に進行されます。  
`/szinrou end` 現在プレイ中のゲームを強制的に終了します。  
`/szinrou select` 性癖入力中に、90秒が経過していなくても、全員が性癖を入力し終えていなくても、強制的に性癖入力を終了し、ゲームを開始します。  
`/szinrou config select_time <seconds>` 性癖入力時間を調整します。  
`/szinrou config thinking_time <seconds>` 会議時間を調整します。  
`/szinrou config werewolf_number <number>` ランダムに選択される人狼の数を調整します。  

## Installation
⚠️ **サーバー環境として、Paper 1.16.5が必須となります。**  
  
性癖人狼の [リリースページ](https://github.com/TeamKun/seihekizinrou/releases/latest) から最新版をダウンロードし、サーバーのpluginsフォルダにダウンロードしたjarファイルを入れてください。  
