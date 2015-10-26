#!/bin/bash

function check_commands() {
   local needed_commands=$1
   local missing_counter=0
   for needed_command in ${needed_commands}; do
     if ! hash "$needed_command" >/dev/null 2>&1; then
       printf "Command not found in PATH: %s\n" "$needed_command" >&2
       ((missing_counter++))
     fi
   done

   if ((missing_counter > 0)); then
     printf "Minimum %d commands are missing in PATH, aborting\n" "$missing_counter" >&2
     exit 1
   fi
}

function clean_diff() {
   local input_file=$1
   local filter_pattern=$2
   local substitution_pattern=$3
   local output_file=$4
   cat ${input_file} \
         | grep -Ee "${filter_pattern}" \
         | sed -E "${substitution_pattern}" > ${output_file}
}

function main() {

   check_commands "unzip diff grep sed mktemp xargs zip"

   local archive_war_new=$1
   local archive_war_old=$2
   local war_new=$(mktemp -d)
   local war_old=$(mktemp -d)
   local diff_file=$(mktemp)
   local to_add=$(mktemp)
   local to_update=$(mktemp)
   local to_delete=$(mktemp)
   local batch_file=$(mktemp)
   local new_files=$(mktemp)
   local target=$(mktemp -d)
   local artifact=handy-warup-diff
   TMP_FILES="${war_new} ${war_old} ${diff_file} ${to_add} ${to_update} ${to_delete} ${batch_file} ${new_files} ${target}"

   unzip -q ${archive_war_new} -d ${war_new}
   unzip -q ${archive_war_old} -d ${war_old}

   diff --recursive --brief ${war_new} ${war_old} > ${diff_file}

   clean_diff ${diff_file} \
              "^Only in ${war_new}" \
              "s;^Only in ${war_new}/?(.*): (.*);add --from=\1/\2 --to=\1/\2;" \
              ${to_add}

   clean_diff ${diff_file} \
              'Files .* and .* differ' \
              "s;Files ${war_new}/(.*) and .* differ;replace --from=\1 --to=\1;" \
              ${to_update}

   clean_diff ${diff_file} \
              "^Only in ${war_old}" \
              "s;^Only in ${war_old}(.*): (.*);rm --from=\1/\2;" \
              ${to_delete}

   cat ${to_add} ${to_update} ${to_delete} > ${batch_file}

   cat ${to_add} ${to_update} \
      | sed -E "s;.* --from=(.*) --to.*;${war_new}/\1;" \
      | xargs -I {} cp --parents --recursive {} ${target}

   pushd ${target}/${war_new}
   cp ${batch_file} ./batch.warup
   zip -q ${artifact} -r .
   popd
   mv ${target}/${war_new}/${artifact}.zip .
   
   function clean {
      rm -rf ${TMP_FILES}
      unset TMP_FILES
   }
   
   trap clean EXIT
}

TMP_FILES=

main ${1} ${2}

exit 0


