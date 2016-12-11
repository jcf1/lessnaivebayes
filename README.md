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
	% sh java_testwith.sh data/love,politics-1.set data/love,politics-2.set
	51374 1-gram * accuracy 65.51% (228/348) data/love,politics-1.set data/love,politics-2.set
	51374 1-gram love precision 41.26% (78/189) data/love,politics-1.set data/love,politics-2.set
	51374 1-gram love recall 89.65% (78/87) data/love,politics-1.set data/love,politics-2.set
	51374 1-gram politics precision 94.33% (150/159) data/love,politics-1.set data/love,politics-2.set
	51374 1-gram politics recall 57.47% (150/261) data/love,politics-1.set data/love,politics-2.set
	51374 2-gram * accuracy 46.55% (162/348) data/love,politics-1.set data/love,politics-2.set
	51374 2-gram love precision 30.73% (79/257) data/love,politics-1.set data/love,politics-2.set
	51374 2-gram love recall 90.80% (79/87) data/love,politics-1.set data/love,politics-2.set
	51374 2-gram politics precision 91.20% (83/91) data/love,politics-1.set data/love,politics-2.set
	51374 2-gram politics recall 31.80% (83/261) data/love,politics-1.set data/love,politics-2.set
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

```
% sh java_testwith.sh data/love,politics-1.set data/love,politics-2.set
51374 1-gram * accuracy 65.51% (228/348) data/love,politics-1.set data/love,politics-2.set
51374 1-gram love precision 41.26% (78/189) data/love,politics-1.set data/love,politics-2.set
51374 1-gram love recall 89.65% (78/87) data/love,politics-1.set data/love,politics-2.set
51374 1-gram politics precision 94.33% (150/159) data/love,politics-1.set data/love,politics-2.set
51374 1-gram politics recall 57.47% (150/261) data/love,politics-1.set data/love,politics-2.set
51374 2-gram * accuracy 46.55% (162/348) data/love,politics-1.set data/love,politics-2.set
51374 2-gram love precision 30.73% (79/257) data/love,politics-1.set data/love,politics-2.set
51374 2-gram love recall 90.80% (79/87) data/love,politics-1.set data/love,politics-2.set
51374 2-gram politics precision 91.20% (83/91) data/love,politics-1.set data/love,politics-2.set
51374 2-gram politics recall 31.80% (83/261) data/love,politics-1.set data/love,politics-2.set
```

Output fields are, in order, a relatively unique ID (a PID), the type of model
used, each category and the statistic computed on the category, a percentage, a
fraction, and the two datasets.

Tests are run in parallel, so they work faster on a computer with multiple
processors.

## java_testcross.sh

Takes any number of files as input, and pairs them all up and runs the above on
each pair. Output is the same, but is longer and can have more than one

Like `java_testwith.sh`, this script also tries to parallelize the tests as
much as possible.
