# Chessalyzer

Very much still in debug. 

A chess application wrapping stockfish. MacOS built, so I assume very much a Mac app only at this point.

Chesspresso

[Chesspresso](http://www.chesspresso.org)

is another dependency. I think this is currently not actively maintained.

I'm considering renaming it to Digital Turk if it gets to an application stage.

Current command line usage...

`javac -p mod -d . src/us/hall/chess/*.java`

`jar -cvf Chessalyzer.jar -C . us module-info.class`

`mv Chessalyzer.jar mod/Chessalyzer.jar`

`java -p mod --add-modules Chessalyzer,chesspresso us.hall.chess.Chessalyzer -o test.csv fics.pgn`

This is how I am currently debugging. If I remember right theres an ant script in the chesspresso related to build that. Some time back I think I tried to make some pgn game file parsing enhancements. It still has problems with some pgn's. These can differ more than you might think.

The functionality I'm trying to add now is cheater detection. The approach I'm attempting is to use machine learning to try and classify players in pgn games as human or computer. If computer, cheaters. If there is some other way of cheating besides using computers I don't know what it is and probably don't want to know. 

I parse the games and check to see how often the player selects the best, second or third best moves according to Stockfish. This is similar to what I understand the US Chess Federation uses to detect cheating. One of my opponents in a email correspondence event sent me their
method. At some point I might compare my results to theirs.

This isn't made any easier because you can mostly find games with human vs. human or computer vs. computer but almost nowhere, at least acknowledged to be, human vs. computer.

I have StockFish evaluate three lines and use three threads - so I'm not sure how it will work on a machine with very limited cores. If a move is chosen that isn't one of the top 3 lines I have StockFish ponder it to get a score.

I intend to generate data with human and computer results for this to create a machine learning model. Then a game can be evaluated against that model to classify the players as either human or computer. What data will be included isn't finalized. The output format being comma separated value, csv, or Weka arff is an option.

Weka

[Weka](https://sourceforge.net/projects/weka/)

At some point I will probably include weka.jar and do some classification with that.

Currently the code has a bug in running the above where it gets a mismatch when Stockfish indicates a bestmove that doesn't match any of the 3 lines that it has been considering.

So this has a very long ways to go.






