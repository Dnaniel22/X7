/*
 * X7 - Cliente de comunicación con Spring Boot
 * --------------------------------------------
 * Este archivo YA NO guarda usuarios, contraseñas, sesiones, productos ni pedidos
 * en localStorage. Toda esa información debe ser administrada por el backend.
 *
 * Endpoints esperados del backend Spring Boot:
 *   POST   /api/auth/login
 *   POST   /api/auth/logout
 *   GET    /api/auth/sesion
 *   POST   /api/usuarios/registro
 *   GET    /api/productos
 *   POST   /api/productos
 *   DELETE /api/productos/{id}
 *   GET    /api/pedidos
 *   GET    /api/pedidos/mis-pedidos
 *   POST   /api/pedidos
 *   PUT    /api/pedidos/{id}/estado
 *   DELETE /api/pedidos/{id}
 */

const X7 = {
  async api(url, options = {}) {
    const config = {
      credentials: "same-origin",
      headers: {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(options.headers || {}),
      },
      ...options,
    };

    const response = await fetch(url, config);

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
      ? await response.json()
      : await response.text();

    if (!response.ok) {
      const message =
        typeof data === "object" && data !== null
          ? data.mensaje || data.error || "Ocurrió un error al procesar la solicitud."
          : data || "Ocurrió un error al procesar la solicitud.";
      throw new Error(message);
    }

    return data;
  },

  // ---------- Autenticación / sesión ----------
  async getSesion() {
    try {
      return await this.api("/api/auth/sesion");
    } catch (error) {
      // Si no hay sesión, el backend puede responder 401/403.
      return null;
    }
  },

  async iniciarSesion(email, password) {
    return this.api("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },

  async cerrarSesion() {
    return this.api("/api/auth/logout", { method: "POST" });
  },

  // ---------- Usuarios ----------
  async registrarUsuario(datos) {
    return this.api("/api/usuarios/registro", {
      method: "POST",
      body: JSON.stringify(datos),
    });
  },

  // ---------- Productos ----------
  async getProductos() {
    return this.api("/api/productos");
  },

  async agregarProducto(producto) {
    return this.api("/api/productos", {
      method: "POST",
      body: JSON.stringify(producto),
    });
  },

  async eliminarProducto(id) {
    return this.api(`/api/productos/${encodeURIComponent(id)}`, {
      method: "DELETE",
    });
  },

  // ---------- Pedidos ----------
  async getPedidos() {
    return this.api("/api/pedidos");
  },

  async pedidosDeCliente() {
    return this.api("/api/pedidos/mis-pedidos");
  },

  async guardarPedido(pedido) {
    return this.api("/api/pedidos", {
      method: "POST",
      body: JSON.stringify(pedido),
    });
  },

  async actualizarEstadoPedido(id, estado) {
    return this.api(`/api/pedidos/${encodeURIComponent(id)}/estado`, {
      method: "PUT",
      body: JSON.stringify({ estado }),
    });
  },

  async eliminarPedido(id) {
    return this.api(`/api/pedidos/${encodeURIComponent(id)}`, {
      method: "DELETE",
    });
  },

  // ---------- Utilidades de interfaz ----------
  escapeHtml(valor) {
    return String(valor ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  },

  async renderAuthMenu(menuEl) {
    if (!menuEl) return;

    const sesion = await this.getSesion();

    if (!sesion) {
      menuEl.innerHTML = `
        <li><a class="dropdown-item" href="/login"><i class="bi bi-box-arrow-in-right me-2"></i>Iniciar sesión</a></li>
        <li><a class="dropdown-item" href="/registro"><i class="bi bi-person-plus me-2"></i>Registrarse</a></li>`;
      return;
    }

    const rol = String(sesion.rol || "").toLowerCase();

    if (rol === "administrador" || rol === "admin") {
      menuEl.innerHTML = `
        <li><a class="dropdown-item" href="/admin/panel"><i class="bi bi-speedometer2 me-2"></i>Panel de Administración</a></li>
        <li><a class="dropdown-item" href="#" id="btnCerrarSesionX7"><i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión</a></li>`;
    } else {
      menuEl.innerHTML = `
        <li><a class="dropdown-item" href="/cliente/panel"><i class="bi bi-speedometer2 me-2"></i>Mi Panel</a></li>
        <li><a class="dropdown-item" href="#" id="btnCerrarSesionX7"><i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión</a></li>`;
    }

    const btn = document.getElementById("btnCerrarSesionX7");
    if (btn) {
      btn.addEventListener("click", async (e) => {
        e.preventDefault();
        try {
          await this.cerrarSesion();
        } finally {
          window.location.href = "/";
        }
      });
    }
  },
};
