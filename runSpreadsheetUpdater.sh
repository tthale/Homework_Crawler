#!/bin/bash
# Since Java is strict on security and disallows changing directories, this
# script is used to do it on its behalf and passes all parameters that are
# thrown at it

# python "C:\Applications\MyEclipse2015CI\Workspaces\Google_Spreadsheet_Updater/fill_spreadsheet.py" Tom_Spreadsheet Jan_14 english,January_11,Brainstorming Handout,HW Due Date,,,Not submitted

debug=true

if [ "$#" -le 4 ]; then
    echo "Syntax: " $0 python "<file path of Python executable>" "<Google spreadsheet name>"  "<Google worksheet name>" "<worksheet row information in CSV format>"
    exit 1
fi

if [ $1 != "python" ]
then
    echo "Is this a python call ? ($1)"
    exit 1
fi

dir_target=`dirname $2`
exec_target=`basename $2`
spreadsheet_name=$3
worksheet_name=$4


cd  $dir_target

# Move arg pointer to where the CSV worksheet row info lives
shift 4
row_info=$1
# Change the Internal Field Separator to that of the lowly comma (Ref: https://unix.stackexchange.com/questions/39680/splitting-bash-command-line-argument)
# This in turn will affect the bash to split args passed to python into separate args
IFS=','
set -f

if [ $debug = true ]; then
    echo CWD=`pwd`
    set -vx	# This actually doesn't output anything when called from Java
fi

# CMD="python $dir_target\\$exec_target $spreadsheet_name $worksheet_name $row_info"
# results=`$CMD`
results=`python $exec_target $spreadsheet_name $worksheet_name $row_info`
if [ $debug = true ]; then
    echo RESULT=$?
    echo CMD=python $dir_target/$exec_target $spreadsheet_name $worksheet_name $row_info
#    sleep 20
fi
set +f

if [ $debug = true ]; then
    set +vx
fi
exit 0

# Below are the previous weak-sauce attempts - pay no attention to them (other than as a monument to failure)

# row_info='"'$1

# while shift; do
#    arg=`echo -n $1`
#     row_info="$row_info"' "'$1'"'
# done

# Escape-Backslash all the white spaces in this CSV conglomeration
# echo "\$_1="$1
# row_info=`echo -n "$1" | sed -e 's/ /\\\\ /g'`

# The row_info is 'tightly packed CSV, so white-space expand it & add quotes around each newly created arg
# also be aware that a column's info could already have multiple words (ugh)
row_info=`echo -n "$row_info" | sed -e 's/,/ /g'`
# row_info=`echo -n "$row_info" | sed -e 's/,/\" \" /g'`

# row_info="$row_info"'"'

echo "row_info=$row_info"

python $exec_target $spreadsheet_name $worksheet_name "$row_info"
# The issue with the above is that $row_info has all args globbed into one - see IFS solution above
