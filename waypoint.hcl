project = "prosanteconnect/pscload-v2"

# Labels can be specified for organizational purposes.
labels = { "domaine" = "psc" }

runner {
    enabled = true
    data_source "git" {
        url = "https://github.com/pam199968/pscload-v2.git"
        ref = "main"
    }
    poll {
        enabled = true
        interval = "24h"
    }
}

# An application to deploy.
app "prosanteconnect/pscload-v2" {
  # the Build step is required and specifies how an application image should be built and published. In this case,
  # we use docker-pull, we simply pull an image as is.
  build {
    use "docker" {
      dockerfile = "${path.app}/${var.dockerfile_path}"
      insecure = true
    }
    # Uncomment below to use a remote docker registry to push your built images.
    registry {
      use "docker" {
        image = "${var.registry_path}/pscload"
        tag   = gitrefpretty()
        insecure = true
        username = var.registry_username
        password = var.registry_password
      }
    }
  }

  # Deploy to Nomad
  deploy {
    use "nomad-jobspec" {
      jobspec = templatefile("${path.app}/pscload-v2.nomad.tpl", {
        datacenter = var.datacenter
        proxy_port = var.proxy_port
        proxy_host = var.proxy_host
        non_proxy_hosts = var.non_proxy_hosts
        registry_path = var.registry_path
      })
    }
  }
}

variable "registry_username" {
  type    = string
  default = dynamic("vault", {
    path = "waypoint/waypoint_runner"
    key  = "registry_user"
  })
}

variable "registry_password" {
  type    = string
  default = dynamic("vault", {
    path = "waypoint/waypoint_runner"
    key  = "registry_password"
  })
}

variable "datacenter" {
  type    = string
  default = "dc1"
}
variable "proxy_port" {
  type = string
  default = ""
}

variable "proxy_host" {
  type = string
  default = ""
}

variable "non_proxy_hosts" {
  type = string
  default = "10.0.0.0/8"
}

variable "dockerfile_path" {
  type = string
  default = "Dockerfile"
}

variable "registry_path" {
  type = string
  default = "registry.repo.docker.proxy-prod-forge.asip.hst.fluxus.net/prosanteconnect"
}
