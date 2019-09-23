set -e

cat <<EOF >> ~/.bashrc
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
EOF

export LC_ALL=C.UTF-8
export LANG=C.UTF-8

node .c1/bin/project-manager
