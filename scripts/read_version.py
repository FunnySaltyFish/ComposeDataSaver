import re
with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()
    match = re.search(r'project\s*=\s*\"([\d\.]+)\"', content)
    if match:
        print('v' + match.group(1))
    else:
        print('v1.0.0')