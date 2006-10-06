rm -rf logs output
mkdir logs
cd logs
touch 9; sleep 1
touch 8; sleep 1
touch 7; sleep 1
touch 6; sleep 1
touch 5; sleep 1
touch 4; sleep 1
touch 3; sleep 1
touch 2; sleep 1
touch 1; sleep 1
cd ..
mkdir output
cd output
touch 1 ; sleep 1
touch 2; sleep 1
touch 3; sleep 1
touch 4; sleep 1
touch 5; sleep 1
touch 6; sleep 1
cd ..
ant
