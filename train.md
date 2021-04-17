How to train models

1. Download and install **git-lfs** from [here](https://docs.github.com/en/github/managing-large-files/installing-git-large-file-storage)
2. `git clone https://github.com/tarekeldeeb/DeepSpeech-Quran.git`
3. `git lfs pull`
4. Generate csv files using `bin/import_quran.py` & `bin/import_quran_tusers.py`
5. Mix the CSV files generated. (Did it manually, for now, could be automated)
6. Increase **--epochs** time on this file `bin/run-quran.sh`, if you want more accuracy(It will need more time to train if **--epochs** are higher, currently, it's **30**)
6. Execute `bin/run-quran.sh` and `bin/run-quran-tusers.sh`