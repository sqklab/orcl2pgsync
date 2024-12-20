PARAMETERS=$(cat init/ssm_.json | jq -r 'to_entries[] | [.key, .value] | @tsv')
echo $PARAMETERS

while IFS=$'\t' read -r key value ;
do
    echo "put $key $value"
    aws ssm put-parameter \
        --endpoint-url=http://localhost:4566 \
        --name $key \
        --value $value \
        --type String
done <<< "$PARAMETERS"
