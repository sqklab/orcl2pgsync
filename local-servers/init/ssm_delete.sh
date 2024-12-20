PARAMETERS=$(cat init/ssm_.json | jq -r 'to_entries[] | [.key, .value] | @tsv')
echo $PARAMETERS

while IFS=$'\t' read -r key value ;
do
    echo "delete $key"
    aws ssm delete-parameter \
        --endpoint-url=http://localhost:4566 \
        --name $key
done <<< "$PARAMETERS"
