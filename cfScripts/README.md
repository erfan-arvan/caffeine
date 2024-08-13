# wpi-njr-debug
Repository to recreate the reported issue on WPI non terminating.


#### Configuration
```
Configure the CF_BINARY in run_wpi.py and /wpi/wpi.sh
```


#### Run
```
cd wpi
python3 run_wpi.py
```
After execution, the resulting `.ajava` files will be stored for each benchmark in the directory `root-dir/dataset/benchmark-directory/src/wpi-iterations`.

The results of running the checker before and after WPI inference can be found in `root-dir/wpi/checkerframework-3.34.0-results-WPI`.

### Dataset
In the `dataset` folder:
- `SimplifiedTestCode` contains the simplified test case.
- `url49526af437_Maria_G_CS325_tgz-pJ8-assignments_farmer_FarmerImplJ8` is the complete sample benchmark for which we encountered the non-termination issue.
- `MoreSamplesOfChallengingBenchmarks` contains more benchmarks with the same issue.
