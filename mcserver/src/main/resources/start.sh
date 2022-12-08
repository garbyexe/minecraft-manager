echo updating dns records
ddclient -daemon=0 -verbose
echo deleating backup
keep=$(curl "http://metadata.google.internal/computeMetadata/v1/instance/attributes/keep" -H "Metadata-Flavor: Google")
if [ "$keep" == "true" ]
then
    echo "keeping backups"
else
    echo "deleting backups"
    gcloud compute snapshots list --filter="sourceDisk=minecraft AND name~automatic-startup" --uri --limit=1 --sort-by=~createTime | xargs gcloud compute snapshots delete --quiet --project=garbysites
fi
echo "creating backup"
gcloud compute snapshots create 'automatic-startup-'"$(date -u +%m-%d-%H-%M)" --project=garbysites --source-disk=minecraft --source-disk-zone=us-central1-a --storage-location=us-central1






gcloud compute snapshots delete "$(gcloud compute snapshots list --filter="sourceDisk=minecraft AND name~automatic-startup" --format "value(name)" --limit=1 --sort-by=~createTime)"