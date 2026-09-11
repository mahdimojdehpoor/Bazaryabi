import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  try {
    const authHeader = req.headers.get("Authorization")
    if (!authHeader) throw new Error("درخواست نامعتبر است")

    const supabaseClient = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_ANON_KEY") ?? "",
      { global: { headers: { Authorization: authHeader } } }
    )
    const { data: { user }, error: userError } = await supabaseClient.auth.getUser()
    if (userError || !user) throw new Error("کاربر احراز هویت نشد")

    let body: { target_user_id?: string } = {}
    try { body = await req.json() } catch (_) { body = {} }

    const adminClient = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
    )

    let userIdToDelete = user.id

    if (body.target_user_id && body.target_user_id !== user.id) {
      const { data: callerProfile } = await adminClient
        .from("profiles").select("role").eq("id", user.id).single()

      const { data: targetProfile } = await adminClient
        .from("profiles").select("owner_id").eq("id", body.target_user_id).single()

      const isOwner = targetProfile?.owner_id === user.id
      const isAdmin = callerProfile?.role === "admin"

      if (!isOwner && !isAdmin) {
        throw new Error("اجازه حذف این حساب را نداری")
      }
      userIdToDelete = body.target_user_id
    }

    const { error: deleteError } = await adminClient.auth.admin.deleteUser(userIdToDelete)
    if (deleteError) throw deleteError

    return new Response(JSON.stringify({ success: true }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 200,
    })
  } catch (e) {
    return new Response(JSON.stringify({ success: false, error: (e as Error).message }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 400,
    })
  }
})
