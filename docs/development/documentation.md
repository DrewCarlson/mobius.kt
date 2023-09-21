# Documentation

Documentation is created with [MkDocs](https://www.mkdocs.org/)
using [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/). MkDocs is configured with `mkdocs.yml` and
documentation source is stored in the `docs` folder.

## Install Python

[Download](https://www.python.org/downloads/) and install the latest version of Python.

??? info "macOS"

    ### macOS
    
    Using [Homebrew](https://brew.sh/)
    
    ```bash
    brew install python
    ```

??? info "Windows"

    ### Windows
    
    [Download](https://www.python.org/downloads/) and install the recommended version
    from [python.org](https://www.python.org/).
    
    Or with [Chocolatey](https://chocolatey.org/)
    
    ```shell
    choco install python
    ```

## Install MkDocs and plugins

Open a new Terminal or Command Prompt window.

```shell
pip install mkdocs mkdocs-material mkdocs-markdownextradata-plugin
```

For more information see the MkDocs [Installation Guide](https://www.mkdocs.org/getting-started/#installation) and the
Material for MkDocs [Installation Guide](https://squidfunk.github.io/mkdocs-material/getting-started/#with-pip).

## Writing Documentation

Documentation source files are written in [Markdown](https://www.markdownguide.org/). For navigation and advanced
formatting features, see the MkDocs [Writing your docs](https://www.mkdocs.org/user-guide/writing-your-docs/) guide and
Material for MkDocs [Reference](https://squidfunk.github.io/mkdocs-material/reference/).

???+ tip "Variables"

    Some dynamic variables are made available, they can be used with the `{{ name }}` syntax.
    Below are the available variables.
    
    | Name        | Value                                                            |
    |-------------|------------------------------------------------------------------|
    | project     | The project name for the repository                              |
    | lib_version | The current release version, for example `2.3.5` (No `v` prefix) |

## View docs locally

To view the docs locally, open a terminal or command prompt and cd into your `{{ project }}` folder then run

```shell
mkdocs serve
```

Your changes will be served at [http://127.0.0.1:8000](http://127.0.0.1:8000). After saving changes, the webpage will
reload automatically.

## Deployment

Changes are deployed automatically for tagged releases.