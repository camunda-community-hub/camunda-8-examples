#!/bin/bash

# Function to generate a random time between 60 and 120 minutes
generate_random_time() {
  min_minutes=60
  max_minutes=120
  wait_minutes=$((min_minutes + RANDOM % (max_minutes - min_minutes + 1)))
  echo $wait_minutes
}

if [[ -z $1 ]] || [[ -z $2 ]] || [[ -z $3 ]]
then 
  echo 'usage: start.sh url_of_process_application run_for_seconds [endless | once]'
  exit 1
else 
  PROCESS_APP_URL=$1
  RUN_SECONDS=$2
  RUN_FOREVER=$3
fi  

if [ $RUN_FOREVER == "endless" ]
then
  echo "run forever"
elif [ $RUN_FOREVER == "once" ]
then
  echo "run once"
else
  echo "Invalid argument. Use 'endless' for an endless loop or 'once' to run the task once."
  exit 1
fi

echo 'run for' $RUN_SECONDS 'seconds'
while [ $RUN_FOREVER == "endless" ] || [ $RUN_FOREVER == "once" ]; do
  while [[ $SECONDS -lt $RUN_SECONDS ]]; do
    CUSTOMER_NUMBER=$(($RANDOM%1000))
    ORDER_TOTAL=$(($RANDOM%1000))
    CARD_NUMBER=$(awk -v paket1=$(($RANDOM%10000)) -v paket2=$(($RANDOM%10000)) \
      -v paket3=$(($RANDOM%10000)) -v paket4=$(($RANDOM%10000)) \
    'BEGIN {printf "%04d %04d %04d %04d", paket1, paket2, paket3, paket4}')
    CVC=$(awk -v cvc=$(($RANDOM%1000)) 'BEGIN {printf "%03d", cvc}')
    MONTH=$(($RANDOM%12+1))
    YEAR=$(($RANDOM%4+$(date +%y)-1))
    EXPIRY_DATE=$(awk -v month="$MONTH" -v year="$YEAR" 'BEGIN {printf "%02d/%02d", month, year}')

    echo $(date +"%Y-%m-%d %H:%M:%S") 'starting with customer' $CUSTOMER_NUMBER ' of order total ' $ORDER_TOTAL ' and payment data' \
      $CARD_NUMBER $CVC $EXPIRY_DATE

    ## start payment process
    curl -L "$PROCESS_APP_URL" \
      -X "POST" -H "Content-Type: application/json" \
      -d "{\"customerId\": \"cust$CUSTOMER_NUMBER\", \
      \"orderTotal\": $ORDER_TOTAL, \
      \"cardNumber\": \"$CARD_NUMBER\", \
      \"cvc\": \"$CVC\", \
      \"expiryDate\": \"$EXPIRY_DATE\"}"
    
    WAIT_TIME=$(awk -v wait_time="$(($RANDOM%20))" 'BEGIN {print wait_time/10}')
    echo 'sleeping for' $WAIT_TIME ' seconds ...'
    sleep $WAIT_TIME
    :
  done

  if [ $RUN_FOREVER == "once" ]
  then
    exit 0
  fi

  wait_time_1=$(generate_random_time)
  echo "waiting for $wait_time_1 minutes ..."
  sleep $((wait_time_1 * 60))
  :
done
