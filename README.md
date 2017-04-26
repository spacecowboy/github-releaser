# github-releaser
Commandline tool for uploading files to GitHub releases

## Usage

```
usage: github-releaser [-h] [-u USER] [-r REPO] [-t TAG] [--token TOKEN] FILE [FILE ...]

Upload artifacts to GitHub releases.

positional arguments:
  FILE                   Files (one or more) two upload to the release

optional arguments:
  -h, --help             show this help message and exit
  -u USER, --user USER   GitHub user (default: )
  -r REPO, --repo REPO   GitHub repository name (default: )
  -t TAG, --tag TAG      Git tag for release (default: )
  --token TOKEN          GitHub account token (default: )
```

All arguments except files can also be given as environment variables (see error messages).

## Example usage

```shell
GITHUB_TOKEN=... github-releaser -u spacecowboy -r github-releaser -t 1.0.0 github-releaser.zip github-releaser.tar
```
