# Files

-	`data/*.csv`: raw data in CSV format
-	`data/*.set`: input data for the classifier
-	`java_*.sh`: scripts to run the program in various ways
-	`scripts/*`: data-mangling scripts

# General Usage

-	Data is collected using `scripts/fetch.sh`
	```
	# generates files happy-1.csv, sad-1.csv, and angry.csv
	% sh scripts/fetch.sh happy sad angry
	```

-	Data is converted to input format using `scripts/datafy.rb`
	```
	# generates a dataset containing #happy, #angry, #sad tweets in *.csv
	ruby scripts/datafy.rb happy,angry,sad *-1.csv > feelings-1.set
	```

-	`java_testwith.sh` accepts two datasets (training then testing), runs the
	program, and reports accuracy statistics for the run according to the
	categories given in the testing dataset
	```
	% sh java_testwith.sh data/happy,sad,angry-1.set data/happy,sad,angry-2.set 
	51147 1-gram * accuracy 66.88% (5414/8095) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 1-gram happy precision 65.51% (2223/3393) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 1-gram happy recall 75.15% (2223/2958) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 1-gram sad precision 78.70% (2251/2860) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 1-gram sad recall 61.68% (2251/3649) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 1-gram angry precision 51.00% (939/1841) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 1-gram angry recall 63.10% (939/1488) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 2-gram * accuracy 47.89% (3877/8095) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 2-gram happy precision 48.35% (894/1849) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 2-gram happy recall 30.22% (894/2958) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 2-gram sad precision 80.51% (1814/2253) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 2-gram sad recall 49.71% (1814/3649) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 2-gram angry precision 29.25% (1168/3992) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	51147 2-gram angry recall 78.49% (1168/1488) data/happy,sad,angry-1.set data/happy,sad,angry-2.set
	```

-	`java_run.sh` and `java_bigram_run.sh` respectively run the program with 
	```
	% cat > try.set <<EOF
	3
	happy
	sad
	angry
	1
	sad I wish I hadn't eaten that cake...
	EOF
	% sh java_run.sh
	# output is in same format
	```


# Script details

## fetch.sh

Uses Twitter API to fetch recent tweets for a particular tag. Requires the ruby
gem ["t"](https://github.com/sferik/t).

```
# generates files happy-1.csv, sad-1.csv, and angry.csv
% sh scripts/fetch.sh happy sad angry
```

## datafy.rb

Converts CSV data into input format. The first paramater is a comma-separated
list of tags of interest; the output dataset has exactly those tags as
categories, and each tweet is sorted into every category whose hashtag it
contains. (If no list is specified, every tag in the input is assumed.)

```
ruby scripts/datafy.rb happy,angry,sad *-1.csv > feelings-1.set
ruby scripts/datafy.rb happy,angry,sad *-2.csv > feelings-2.set
```

## java_testwith.sh

Takes two arguments: a training set and a testing set. Trains on the training
set and runs on the testing set in both unigram and bigram modes, then reports
on the accuracy, precision, and recall.

## java_testcross.sh

Takes any number of files as input, and pairs them all up and runs the above on
each pair.
