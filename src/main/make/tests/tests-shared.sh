#!/usr/bin/env bash

FC=/tmp/freechains
./tests-clean.sh

# 8400 (correct password)
freechains host create $FC/8400 8400
freechains host start $FC/8400 &
sleep 0.5
freechains --host=localhost:8400 chain create /0 shared correct

# 8401 (wrong password)
freechains host create $FC/8401 8401
freechains host start $FC/8401 &
sleep 0.5
freechains --host=localhost:8401 chain create /0 shared wrong

# 8402 (correct password)
freechains host create $FC/8402 8402
freechains host start $FC/8402 &
sleep 0.5
freechains --host=localhost:8402 chain create /0 shared correct

# get genesis block of each host
g0=`freechains --host=localhost:8400 chain genesis /0`
g1=`freechains --host=localhost:8401 chain genesis /0`
g2=`freechains --host=localhost:8402 chain genesis /0`

# compare them
! diff -q <(echo "$g0") <(echo "$g1") || exit 1
diff <(echo "$g0") <(echo "$g2")      || exit 1

# put to 8400, send to 8401 (fail) 8402 (succees)
freechains --host=localhost:8400 chain put /0 inline utf8 Hello_World
freechains --host=localhost:8400 chain send /0 localhost:8401  # FAIL
freechains --host=localhost:8400 chain send /0 localhost:8402  # SUCCESS

# compare them
! diff -q $FC/8400/chains/0 $FC/8401/chains/0/ || exit 1
diff $FC/8400/chains/0 $FC/8402/chains/0/      || exit 1

# stop nodes
freechains host stop --host=localhost:8400 &
freechains host stop --host=localhost:8401 &
freechains host stop --host=localhost:8402 &
sleep 0.5

echo
echo "=== ALL TESTS PASSED ==="
echo